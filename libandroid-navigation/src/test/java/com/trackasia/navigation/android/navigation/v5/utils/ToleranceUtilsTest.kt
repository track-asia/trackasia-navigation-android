package com.trackasia.navigation.android.navigation.v5.utils

import org.junit.Assert
import org.junit.Test
import com.trackasia.geojson.LineString
import com.trackasia.geojson.utils.PolylineUtils
import com.trackasia.navigation.android.navigation.v5.BaseTest
import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigationOptions
import com.trackasia.turf.TurfConstants
import com.trackasia.turf.TurfMeasurement

class ToleranceUtilsTest : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userFarAwayFromIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val stepPoints = PolylineUtils.decode(
            route.geometry, Constants.PRECISION_6
        )
        val midPoint = TurfMeasurement.midpoint(stepPoints[0], stepPoints[1])

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            midPoint,
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        Assert.assertEquals(25.0, tolerance, DELTA)
    }


    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userCloseToIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance - 39
        val lineString = LineString.fromPolyline(
            route.geometry, Constants.PRECISION_6
        )
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        Assert.assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun dynamicRerouteDistanceTolerance_userJustPastTheIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance
        val lineString = LineString.fromPolyline(
            route.geometry, Constants.PRECISION_6
        )
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfConstants.UNIT_METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        Assert.assertEquals(50.0, tolerance, DELTA)
    }
}