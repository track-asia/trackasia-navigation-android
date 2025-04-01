package com.trackasia.navigation.core.utils

import com.trackasia.geojson.model.Point
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.geojson.turf.TurfMeasurement
import com.trackasia.geojson.turf.TurfMisc
import com.trackasia.geojson.turf.TurfUnit
import com.trackasia.navigation.core.models.StepIntersection
import kotlin.jvm.JvmStatic

object ToleranceUtils {

    /**
     * Reduce the offRouteMinimumDistanceMetersBeforeWrongDirection if we are close to an intersection.
     * You can define these values in the navigationOptions
     */
    @JvmStatic
    fun dynamicOffRouteRadiusTolerance(
        snappedPoint: Point,
        routeProgress: RouteProgress,
        navigationOptions: TrackAsiaNavigationOptions
    ): Double {
        val intersections = routeProgress.currentLegProgress.currentStepProgress.intersections
        if (intersections != null && intersections.size >= 2) {
            val closestIntersectionFeature = TurfMisc.nearestPointOnLine(
                snappedPoint,
                intersections.map(StepIntersection::location)
            )

            val closestIntersection = closestIntersectionFeature.geometry as Point
            if (closestIntersection == snappedPoint) {
                return navigationOptions.offRouteThresholdRadiusMeters
            }

            val distanceToNextIntersection = TurfMeasurement.distance(
                snappedPoint,
                closestIntersection,
                TurfUnit.METERS
            )

            if (distanceToNextIntersection <= navigationOptions.maneuverZoneRadius) {
                return navigationOptions.offRouteThresholdRadiusMeters / 2
            }
        }

        return navigationOptions.offRouteThresholdRadiusMeters
    }
}