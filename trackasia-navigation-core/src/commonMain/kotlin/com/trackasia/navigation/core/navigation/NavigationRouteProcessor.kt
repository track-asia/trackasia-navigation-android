package com.trackasia.navigation.core.navigation

import com.trackasia.geojson.model.Point
import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.models.LegStep
import com.trackasia.navigation.core.models.RouteLeg
import com.trackasia.navigation.core.models.StepIntersection
import com.trackasia.navigation.core.navigation.NavigationHelper.checkBearingForStepCompletion
import com.trackasia.navigation.core.navigation.NavigationHelper.createCurrentAnnotation
import com.trackasia.navigation.core.navigation.NavigationHelper.createDistancesToIntersections
import com.trackasia.navigation.core.navigation.NavigationHelper.createIntersectionsList
import com.trackasia.navigation.core.navigation.NavigationHelper.decodeStepPoints
import com.trackasia.navigation.core.navigation.NavigationHelper.findCurrentIntersection
import com.trackasia.navigation.core.navigation.NavigationHelper.findUpcomingIntersection
import com.trackasia.navigation.core.navigation.NavigationHelper.increaseIndex
import com.trackasia.navigation.core.navigation.NavigationHelper.legDistanceRemaining
import com.trackasia.navigation.core.navigation.NavigationHelper.routeDistanceRemaining
import com.trackasia.navigation.core.navigation.NavigationHelper.stepDistanceRemaining
import com.trackasia.navigation.core.offroute.OffRoute
import com.trackasia.navigation.core.offroute.OffRouteCallback
import com.trackasia.navigation.core.offroute.OffRouteDetector
import com.trackasia.navigation.core.routeprogress.CurrentLegAnnotation
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.navigation.core.utils.RouteUtils
import kotlin.jvm.JvmField

open class NavigationRouteProcessor(
    private val routeUtils: RouteUtils
) : OffRouteCallback {

    @JvmField
    var routeProgress: RouteProgress? = null
    private var currentStepPoints: List<Point>? = null
    private var upcomingStepPoints: List<Point>? = null
    private var currentIntersections: List<StepIntersection>? = null
    private var currentIntersectionDistances: Map<StepIntersection, Double>? = null
    private var currentLeg: RouteLeg? = null
    private var currentStep: LegStep? = null
    private var upcomingStep: LegStep? = null
    private var currentLegAnnotation: CurrentLegAnnotation? = null
    private var indices: NavigationIndices =
        NavigationIndices(legIndex = FIRST_LEG_INDEX, stepIndex = FIRST_STEP_INDEX)
    private var stepDistanceRemaining = 0.0
    private var shouldIncreaseIndex = false
    private var shouldUpdateToIndex: NavigationIndices? = null

    override fun onShouldIncreaseIndex() {
        shouldIncreaseIndex = true
    }

    override fun onShouldUpdateToIndex(legIndex: Int, stepIndex: Int) {
        shouldUpdateToIndex = NavigationIndices(legIndex = legIndex, stepIndex = stepIndex)
        onShouldIncreaseIndex()
    }

    /**
     * Will take a given location update and create a new [RouteProgress]
     * based on our calculations of the distances remaining.
     *
     *
     * Also in charge of detecting if a step / leg has finished and incrementing the
     * indices if needed ([NavigationRouteProcessor.advanceIndices] handles
     * the decoding of the next step point list).
     *
     * @param navigation for the current route / options
     * @param location   for step / leg / route distance remaining
     * @return new route progress along the route
     */
    fun buildNewRouteProgress(navigation: TrackAsiaNavigation, location: Location): RouteProgress {
        val directionsRoute = navigation.route!!
        val options = navigation.options
        val completionOffset = options.maxTurnCompletionOffset
        val maneuverZoneRadius = options.maneuverZoneRadius
        val newRoute = checkNewRoute(navigation)
        stepDistanceRemaining = calculateStepDistanceRemaining(location, directionsRoute)
        if (!newRoute && routeProgress != null) {
            checkManeuverCompletion(
                navigation,
                location,
                directionsRoute,
                completionOffset,
                maneuverZoneRadius
            )
        }
        routeProgress = assembleRouteProgress(directionsRoute)
        return routeProgress!!
    }

    /**
     * If the [OffRouteCallback.onShouldIncreaseIndex] has been called by the
     * [OffRouteDetector], shouldIncreaseIndex
     * will be true and the [NavigationIndices] index needs to be increased by one.
     *
     * @param navigation to get the next [LegStep.geometry] and off-route engine
     */
    fun checkIncreaseIndex(navigation: TrackAsiaNavigation) {
        if (shouldIncreaseIndex) {
            advanceIndices(navigation)
            shouldIncreaseIndex = false
            shouldUpdateToIndex = null
        }
    }

    /**
     * Checks if the route provided is a new route. If it is, all [RouteProgress]
     * data and [NavigationIndices] needs to be reset.
     *
     * @param trackAsiaNavigation to get the current route and off-route engine
     * @return Whether or not a route progress is already set and [RouteUtils] determines this is a new route
     */
    private fun checkNewRoute(trackAsiaNavigation: TrackAsiaNavigation): Boolean {
        return trackAsiaNavigation.route?.let { directionsRoute ->
            val newRoute = routeUtils.isNewRoute(routeProgress, directionsRoute)
            if (newRoute) {
                createFirstIndices(trackAsiaNavigation)
                currentLegAnnotation = null
            }
            newRoute
        } ?: false
    }

    /**
     * Given a location update, calculate the current step distance remaining.
     *
     * @param location        for current coordinates
     * @param directionsRoute for current [LegStep]
     * @return distance remaining in meters
     */
    private fun calculateStepDistanceRemaining(
        location: Location,
        directionsRoute: DirectionsRoute
    ): Double {
        return stepDistanceRemaining(
            location, indices.legIndex, indices.stepIndex, directionsRoute, currentStepPoints!!
        )
    }

    private fun checkManeuverCompletion(
        navigation: TrackAsiaNavigation, location: Location, directionsRoute: DirectionsRoute,
        completionOffset: Double, maneuverZoneRadius: Double
    ) {
        val withinManeuverRadius = stepDistanceRemaining < maneuverZoneRadius
        val bearingMatchesManeuver = checkBearingForStepCompletion(
            location, routeProgress!!, stepDistanceRemaining, completionOffset
        )
        val forceIncreaseIndices = stepDistanceRemaining == 0.0 && !bearingMatchesManeuver

        if ((bearingMatchesManeuver && withinManeuverRadius) || forceIncreaseIndices) {
            advanceIndices(navigation)
            stepDistanceRemaining = calculateStepDistanceRemaining(location, directionsRoute)
        }
    }

    /**
     * Increases the step index in [NavigationIndices] by 1.
     *
     *
     * Decodes the step points for the new step and clears the distances from
     * maneuver stack, as the maneuver has now changed.
     *
     * @param trackAsiaNavigation to get the next [LegStep.geometry] and [OffRoute]
     */
    private fun advanceIndices(trackAsiaNavigation: TrackAsiaNavigation) {
        val newIndices: NavigationIndices =
            shouldUpdateToIndex ?: increaseIndex(routeProgress!!, indices)

        if (newIndices.legIndex != indices.legIndex) {
            currentLegAnnotation = null
        }
        indices = newIndices
        processNewIndex(trackAsiaNavigation)
    }

    /**
     * Initializes or resets the [NavigationIndices] for a new route received.
     *
     * @param trackAsiaNavigation to get the next [LegStep.geometry] and [OffRoute]
     */
    private fun createFirstIndices(trackAsiaNavigation: TrackAsiaNavigation) {
        indices = NavigationIndices(FIRST_LEG_INDEX, FIRST_STEP_INDEX)
        processNewIndex(trackAsiaNavigation)
    }

    /**
     * Called after [NavigationHelper.increaseIndex].
     *
     *
     * Processes all new index-based data that is
     * needed for [NavigationRouteProcessor.assembleRouteProgress].
     *
     * @param trackAsiaNavigation for the current route
     */
    private fun processNewIndex(trackAsiaNavigation: TrackAsiaNavigation) {
        val route = trackAsiaNavigation.route!!
        val legIndex = indices.legIndex
        val stepIndex = indices.stepIndex
        val upcomingStepIndex = stepIndex + ONE_INDEX
        if (route.legs.size <= legIndex || route.legs[legIndex].steps.size <= stepIndex) {
            // This catches a potential race condition when the route is changed, before the new index is processed
            createFirstIndices(trackAsiaNavigation)
            return
        }
        updateSteps(route, legIndex, stepIndex, upcomingStepIndex)
        updateStepPoints(route, legIndex, stepIndex, upcomingStepIndex)
        updateIntersections()
        clearManeuverDistances(trackAsiaNavigation.offRouteEngine)
    }

    private fun assembleRouteProgress(route: DirectionsRoute): RouteProgress {
        val legIndex = indices.legIndex
        val stepIndex = indices.stepIndex

        val legDistanceRemaining =
            legDistanceRemaining(stepDistanceRemaining, legIndex, stepIndex, route)
        val routeDistanceRemaining = routeDistanceRemaining(legDistanceRemaining, legIndex, route)
        currentLegAnnotation = createCurrentAnnotation(
            currentLegAnnotation,
            currentLeg!!, legDistanceRemaining
        )
        val stepDistanceTraveled = currentStep!!.distance - stepDistanceRemaining

        val currentIntersection = findCurrentIntersection(
            currentIntersections!!, currentIntersectionDistances!!, stepDistanceTraveled
        )
        val upcomingIntersection = findUpcomingIntersection(
            currentIntersections!!, upcomingStep, currentIntersection!!
        )

        return RouteProgress(
            stepDistanceRemaining = stepDistanceRemaining,
            legDistanceRemaining = legDistanceRemaining,
            distanceRemaining = routeDistanceRemaining,
            directionsRoute = route,
            currentStepPoints = currentStepPoints!!,
            upcomingStepPoints = upcomingStepPoints,
            stepIndex = stepIndex,
            legIndex = legIndex,
            intersections = currentIntersections,
            currentIntersection = currentIntersection,
            upcomingIntersection = upcomingIntersection,
            intersectionDistancesAlongStep = currentIntersectionDistances,
            currentLegAnnotation = currentLegAnnotation,
        )
    }

    private fun updateSteps(
        route: DirectionsRoute,
        legIndex: Int,
        stepIndex: Int,
        upcomingStepIndex: Int
    ) {
        currentLeg = route.legs[legIndex]
        val steps = currentLeg?.steps
        currentStep = steps!![stepIndex]
        upcomingStep =
            if (upcomingStepIndex < steps.size - ONE_INDEX) steps[upcomingStepIndex] else null
    }

    private fun updateStepPoints(
        route: DirectionsRoute,
        legIndex: Int,
        stepIndex: Int,
        upcomingStepIndex: Int
    ) {
        currentStepPoints =
            decodeStepPoints(route, currentStepPoints ?: emptyList(), legIndex, stepIndex)
        upcomingStepPoints = decodeStepPoints(route, emptyList(), legIndex, upcomingStepIndex)
    }

    private fun updateIntersections() {
        currentIntersections = createIntersectionsList(currentStep!!, upcomingStep)
        currentIntersectionDistances =
            createDistancesToIntersections(currentStepPoints!!, currentIntersections!!)
    }

    private fun clearManeuverDistances(offRoute: OffRoute) {
        (offRoute as? OffRouteDetector)?.clearDistancesAwayFromManeuver()
    }

    companion object {
        private const val FIRST_LEG_INDEX = 0
        private const val FIRST_STEP_INDEX = 0
        private const val ONE_INDEX = 1
    }
}
