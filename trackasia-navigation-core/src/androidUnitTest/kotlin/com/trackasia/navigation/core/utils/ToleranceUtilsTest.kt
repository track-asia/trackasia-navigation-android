package com.trackasia.navigation.core.utils

import com.trackasia.geojson.model.LineString
import com.trackasia.geojson.model.Point
import com.trackasia.navigation.core.BaseTest
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions
import com.trackasia.geojson.turf.TurfMeasurement
import com.trackasia.geojson.turf.TurfUnit
import com.trackasia.geojson.utils.PolylineUtils
import com.trackasia.navigation.core.models.StepIntersection
import kotlin.test.Test
import kotlin.test.assertEquals

class ToleranceUtilsTest : BaseTest() {

    @Test
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

        assertEquals(25.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_userCloseToIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance - 39
        val lineString = LineString(route.geometry, Constants.PRECISION_6)
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfUnit.METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_userJustPastTheIntersection() {
        val route = buildTestDirectionsRoute()
        val routeProgress = buildDefaultTestRouteProgress()
        val distanceToIntersection = route.distance
        val lineString = LineString(route.geometry, Constants.PRECISION_6)
        val closePoint =
            TurfMeasurement.along(lineString, distanceToIntersection, TurfUnit.METERS)

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            closePoint,
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_noIntersections() {
        val routeProgress = buildDefaultTestRouteProgress().copy(intersections = listOf())

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            routeProgress.currentStepPoints[0],
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }

    @Test
    fun dynamicRerouteDistanceTolerance_singleIntersection() {
        val routeProgress = buildDefaultTestRouteProgress().copy(
            intersections = listOf(StepIntersection(location = Point(-122.416686, 37.783425)))
        )

        val tolerance = ToleranceUtils.dynamicOffRouteRadiusTolerance(
            routeProgress.currentStepPoints[0],
            routeProgress,
            TrackAsiaNavigationOptions()
        )

        assertEquals(50.0, tolerance, DELTA)
    }
}