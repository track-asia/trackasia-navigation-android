package com.trackasia.navigation.android.navigation.v5.utils

import com.trackasia.geojson.Point
import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigationOptions
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress
import com.trackasia.turf.TurfClassification
import com.trackasia.turf.TurfConstants
import com.trackasia.turf.TurfMeasurement

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
        if (!intersections.isNullOrEmpty()) {
            val intersectionsPoints: MutableList<Point> = ArrayList()
            for (intersection in intersections) {
                intersectionsPoints.add(intersection.location)
            }

            val closestIntersection = TurfClassification.nearestPoint(snappedPoint, intersectionsPoints)
            if (closestIntersection == snappedPoint) {
                return navigationOptions.offRouteThresholdRadiusMeters
            }

            val distanceToNextIntersection = TurfMeasurement.distance(
                snappedPoint,
                closestIntersection,
                TurfConstants.UNIT_METERS
            )

            if (distanceToNextIntersection <= navigationOptions.maneuverZoneRadius) {
                return navigationOptions.offRouteThresholdRadiusMeters / 2
            }
        }

        return navigationOptions.offRouteThresholdRadiusMeters
    }
}