package com.trackasia.navigation.android.navigation.v5.navigation

import android.location.Location
import android.os.Handler
import android.os.Message
import com.trackasia.navigation.android.navigation.v5.milestone.Milestone
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.buildSnappedLocation
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.checkMilestones
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.isUserOffRoute
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress

open class RouteProcessorHandlerCallback(
    private val routeProcessor: NavigationRouteProcessor,
    private val responseHandler: Handler,
    private val listener: RouteProcessorBackgroundThread.Listener
) : Handler.Callback {

    override fun handleMessage(msg: Message): Boolean {
        return (msg.obj as? NavigationLocationUpdate)?.let { update ->
            handleRequest(update)
            true
        } ?: false
    }

    /**
     * Takes a new location model and runs all related engine checks against it
     * (off-route, milestones, snapped location, and faster-route).
     *
     *
     * After running through the engines, all data is submitted to [NavigationService] via
     * [RouteProcessorBackgroundThread.Listener].
     *
     * @param update hold location, navigation (with options), and distances away from maneuver
     */
    private fun handleRequest(update: NavigationLocationUpdate) {
        val trackAsiaNavigation = update.trackAsiaNavigation
        val rawLocation = update.location
        val routeProgress = routeProcessor.buildNewRouteProgress(trackAsiaNavigation, rawLocation)

        val userOffRoute = determineUserOffRoute(update, trackAsiaNavigation, routeProgress)
        val milestones = findTriggeredMilestones(trackAsiaNavigation, routeProgress)
        val location = findSnappedLocation(
                trackAsiaNavigation,
                rawLocation,
                routeProgress,
                userOffRoute
            )

        val finalRouteProgress = updateRouteProcessorWith(routeProgress)
        sendUpdateToListener(userOffRoute, milestones, location, finalRouteProgress)
    }

    private fun findTriggeredMilestones(
        trackAsiaNavigation: TrackAsiaNavigation,
        routeProgress: RouteProgress
    ): List<Milestone> {
        val previousRouteProgress = routeProcessor.routeProgress
        return checkMilestones(previousRouteProgress, routeProgress, trackAsiaNavigation)
    }

    private fun findSnappedLocation(
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

    private fun determineUserOffRoute(
        navigationLocationUpdate: NavigationLocationUpdate,
        trackAsiaNavigation: TrackAsiaNavigation,
        routeProgress: RouteProgress
    ): Boolean {
        val userOffRoute = isUserOffRoute(
            navigationLocationUpdate, routeProgress,
            routeProcessor
        )
        routeProcessor.checkIncreaseIndex(trackAsiaNavigation)
        return userOffRoute
    }

    private fun updateRouteProcessorWith(routeProgress: RouteProgress): RouteProgress {
        routeProcessor.routeProgress = routeProgress
        return routeProgress
    }

    private fun sendUpdateToListener(
        userOffRoute: Boolean,
        milestones: List<Milestone>,
        location: Location,
        finalRouteProgress: RouteProgress
    ) {
        responseHandler.post {
            listener.onNewRouteProgress(location, finalRouteProgress)
            listener.onMilestoneTrigger(milestones, finalRouteProgress)
            listener.onUserOffRoute(location, userOffRoute)
        }
    }
}
