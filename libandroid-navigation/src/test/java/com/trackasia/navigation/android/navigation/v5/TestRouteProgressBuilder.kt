package com.trackasia.navigation.android.navigation.v5

import com.trackasia.geojson.Point
import com.trackasia.geojson.utils.PolylineUtils
import com.trackasia.navigation.android.navigation.v5.models.DirectionsRoute
import com.trackasia.navigation.android.navigation.v5.models.LegStep
import com.trackasia.navigation.android.navigation.v5.models.StepIntersection
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.createDistancesToIntersections
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.createIntersectionsList
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.findCurrentIntersection
import com.trackasia.navigation.android.navigation.v5.navigation.NavigationHelper.findUpcomingIntersection
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress
import com.trackasia.navigation.android.navigation.v5.utils.Constants

internal class TestRouteProgressBuilder {
    @Throws(Exception::class)
    fun buildDefaultTestRouteProgress(testRoute: DirectionsRoute): RouteProgress {
        return buildTestRouteProgress(
            testRoute, 100.0, 100.0,
            100.0, 0, 0
        )
    }

    @Throws(Exception::class)
    fun buildTestRouteProgress(
        route: DirectionsRoute,
        stepDistanceRemaining: Double,
        legDistanceRemaining: Double,
        distanceRemaining: Double,
        stepIndex: Int,
        legIndex: Int
    ): RouteProgress {
        val steps = route.legs[legIndex].steps
        val currentStep = steps[stepIndex]
        val currentStepPoints = buildCurrentStepPoints(currentStep)
        val upcomingStepIndex = stepIndex + 1
        var upcomingStepPoints: List<Point>? = null
        var upcomingStep: LegStep? = null
        if (upcomingStepIndex < steps.size) {
            upcomingStep = steps[upcomingStepIndex]
            val upcomingStepGeometry = upcomingStep.geometry
            upcomingStepPoints = buildStepPointsFromGeometry(upcomingStepGeometry)
        }
        val intersections: List<StepIntersection> =
            createIntersectionsList(currentStep, upcomingStep)
        val intersectionDistances = createDistancesToIntersections(
            currentStepPoints, intersections
        )

        val currentIntersection = createCurrentIntersection(
            stepDistanceRemaining,
            currentStep,
            intersections,
            intersectionDistances
        )
        val upcomingIntersection = createUpcomingIntersection(
            upcomingStep,
            intersections,
            currentIntersection!!
        )

        return RouteProgress(
            stepDistanceRemaining = stepDistanceRemaining,
            legDistanceRemaining = legDistanceRemaining,
            distanceRemaining = distanceRemaining,
            directionsRoute = route,
            currentStepPoints = currentStepPoints,
            upcomingStepPoints = upcomingStepPoints,
            intersections = intersections,
            currentIntersection = currentIntersection,
            upcomingIntersection = upcomingIntersection,
            intersectionDistancesAlongStep = intersectionDistances,
            stepIndex = stepIndex,
            legIndex = legIndex,
            currentLegAnnotation = null
        )
    }

    private fun buildCurrentStepPoints(currentStep: LegStep): List<Point> {
        val currentStepGeometry = currentStep.geometry
        return buildStepPointsFromGeometry(currentStepGeometry)
    }

    private fun createCurrentIntersection(
        stepDistanceRemaining: Double, currentStep: LegStep,
        intersections: List<StepIntersection>,
        intersectionDistances: Map<StepIntersection, Double>
    ): StepIntersection? {
        val stepDistanceTraveled = currentStep.distance - stepDistanceRemaining
        return findCurrentIntersection(
            intersections,
            intersectionDistances, stepDistanceTraveled
        )
    }

    private fun createUpcomingIntersection(
        upcomingStep: LegStep?,
        intersections: List<StepIntersection>,
        currentIntersection: StepIntersection
    ): StepIntersection? {
        return findUpcomingIntersection(
            intersections,
            upcomingStep,
            currentIntersection
        )
    }

    private fun buildStepPointsFromGeometry(stepGeometry: String): List<Point> {
        return PolylineUtils.decode(stepGeometry, Constants.PRECISION_6)
    }
}
