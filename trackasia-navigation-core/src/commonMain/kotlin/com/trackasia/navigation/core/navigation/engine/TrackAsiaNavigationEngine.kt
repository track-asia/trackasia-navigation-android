package com.trackasia.navigation.core.navigation.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.location.LocationValidator
import com.trackasia.navigation.core.location.engine.LocationEngine
import com.trackasia.navigation.core.milestone.Milestone
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation
import com.trackasia.navigation.core.navigation.NavigationEventDispatcher
import com.trackasia.navigation.core.navigation.NavigationHelper.buildSnappedLocation
import com.trackasia.navigation.core.navigation.NavigationHelper.checkMilestones
import com.trackasia.navigation.core.navigation.NavigationHelper.isUserOffRoute
import com.trackasia.navigation.core.navigation.NavigationRouteProcessor
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.navigation.core.utils.RouteUtils

/**
 * Default implementation for [NavigationEngine] which is responsible for fetching location updates
 * and processing them to set the current navigation state.
 */
open class TrackAsiaNavigationEngine(
    private val trackAsiaNavigation: TrackAsiaNavigation,
    private val routeUtils: RouteUtils,
    private val locationValidator: LocationValidator = LocationValidator(trackAsiaNavigation.options.locationAcceptableAccuracyInMetersThreshold),
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : NavigationEngine {
    private val locationEngine: LocationEngine
        get() = trackAsiaNavigation.locationEngine

    private val eventDispatcher: NavigationEventDispatcher
        get() = trackAsiaNavigation.eventDispatcher

    private val navigationRouteProcessor = NavigationRouteProcessor(routeUtils)

    private var collectLocationJob: Job? = null

    /**
     * Start navigation for the given route.
     *
     * This call will starting listening to location updates and process this data to update to the current navigation state.
     * This will run until the [stopNavigation] is called.
     */
    override fun startNavigation(route: DirectionsRoute) {
        collectLocationJob?.cancel() // Cancel previous started run

        collectLocationJob = backgroundScope.launch {
            processLocationUpdate(
                locationEngine.getLastLocation() ?: routeUtils.createFirstLocationFromRoute(route)
            )

            locationEngine.listenToLocation(
                LocationEngine.Request(
                    minIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                    maxIntervalMilliseconds = LOCATION_ENGINE_INTERVAL,
                )
            ).collect(::processLocationUpdate)
        }
    }

    /**
     * Stop and cancel the current running navigation.
     *
     * This means listening to the location updates are stopped and not consumed anymore.
     */
    override fun stopNavigation() {
        collectLocationJob?.cancel()
        collectLocationJob = null
    }

    /**
     * Check if the navigation is running
     *
     * @return true if the navigation is running, false otherwise.
     */
    override fun isRunning(): Boolean {
        return collectLocationJob?.isActive == true
    }

    /**
     * Takes a new location model and runs all related engine checks against it
     * (off-route, milestones, snapped location, and faster-route).
     *
     * After running through the engines, all data is submitted to [NavigationEventDispatcher].
     *
     * @param rawLocation hold location, navigation (with options), and distances away from maneuver
     */
    protected fun processLocationUpdate(rawLocation: Location) {
        if (!locationValidator.isValidUpdate(rawLocation)) {
            return
        }

        val routeProgress = navigationRouteProcessor
            .buildNewRouteProgress(trackAsiaNavigation, rawLocation)

        val userOffRoute = determineUserOffRoute(trackAsiaNavigation, rawLocation, routeProgress)
        val milestones = findTriggeredMilestones(trackAsiaNavigation, routeProgress)
        val location = findSnappedLocation(
            trackAsiaNavigation,
            rawLocation,
            routeProgress,
            userOffRoute
        )

        val finalRouteProgress = updateRouteProcessorWith(routeProgress)
        dispatchUpdate(userOffRoute, milestones, location, finalRouteProgress)
    }

    protected fun findTriggeredMilestones(
        trackAsiaNavigation: TrackAsiaNavigation,
        routeProgress: RouteProgress
    ): List<Milestone> {
        val previousRouteProgress = navigationRouteProcessor.routeProgress
        return checkMilestones(previousRouteProgress, routeProgress, trackAsiaNavigation)
    }

    protected fun findSnappedLocation(
        trackAsiaNavigation: TrackAsiaNavigation,
        rawLocation: Location,
        routeProgress: RouteProgress,
        userOffRoute: Boolean
    ): Location {
        val snapToRouteEnabled = trackAsiaNavigation.options.snapToRoute
        return buildSnappedLocation(
            trackAsiaNavigation,
            snapToRouteEnabled,
            rawLocation,
            routeProgress,
            userOffRoute
        )
    }

    protected fun determineUserOffRoute(
        trackAsiaNavigation: TrackAsiaNavigation,
        location: Location,
        routeProgress: RouteProgress
    ): Boolean {
        val userOffRoute = isUserOffRoute(
            trackAsiaNavigation,
            location,
            routeProgress,
            navigationRouteProcessor
        )
        navigationRouteProcessor.checkIncreaseIndex(trackAsiaNavigation)
        return userOffRoute
    }

    protected fun updateRouteProcessorWith(routeProgress: RouteProgress): RouteProgress {
        navigationRouteProcessor.routeProgress = routeProgress
        return routeProgress
    }

    protected fun dispatchUpdate(
        userOffRoute: Boolean,
        milestones: List<Milestone>,
        location: Location,
        routeProgress: RouteProgress
    ) {
        mainScope.launch {
            dispatchRouteProgress(location, routeProgress)
            dispatchTriggeredMilestones(milestones, routeProgress)
            dispatchOffRoute(location, userOffRoute)
        }
    }

    protected fun dispatchRouteProgress(location: Location, routeProgress: RouteProgress) {
        eventDispatcher.onProgressChange(location, routeProgress)
    }

    protected fun dispatchTriggeredMilestones(
        triggeredMilestones: List<Milestone>,
        routeProgress: RouteProgress
    ) {
        for (milestone in triggeredMilestones) {
            val instruction = milestone.getInstruction()?.buildInstruction(routeProgress)
            eventDispatcher.onMilestoneEvent(routeProgress, instruction, milestone)
        }
    }

    protected fun dispatchOffRoute(location: Location, isUSerOffRoute: Boolean) {
        if (isUSerOffRoute) {
            eventDispatcher.onUserOffRoute(location)
        }
    }

    companion object {
        const val LOCATION_ENGINE_INTERVAL = 1000L
    }
}
