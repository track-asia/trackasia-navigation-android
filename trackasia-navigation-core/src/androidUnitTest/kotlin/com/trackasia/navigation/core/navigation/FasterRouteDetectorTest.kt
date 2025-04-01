package com.trackasia.navigation.core.navigation

import io.mockk.mockk
import com.trackasia.navigation.core.BaseTest
import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.route.FasterRoute
import com.trackasia.navigation.core.route.FasterRouteDetector
import com.trackasia.navigation.core.routeprogress.RouteProgress
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class FasterRouteDetectorTest : BaseTest() {

    @Test
    @Throws(Exception::class)
    fun sanity() {
        val fasterRouteDetector = FasterRouteDetector(TrackAsiaNavigationOptions())

        assertNotNull(fasterRouteDetector)
    }

    @Test
    @Throws(Exception::class)
    fun defaultFasterRouteEngine_didGetAddedOnInitialization() {
        val navigation = buildNavigationWithFasterRouteEnabled()

        assertNotNull(navigation.fasterRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun addFasterRouteEngine_didGetAdded() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = mockk<FasterRoute>()

        navigation.fasterRouteEngine = fasterRouteEngine

        assertEquals(navigation.fasterRouteEngine, fasterRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun onFasterRouteResponse_isFasterRouteIsTrue() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine
        var currentProgress = obtainDefaultRouteProgress()
        val longerRoute: DirectionsRoute = currentProgress.directionsRoute.copy(
            duration = 10000000.0
        )
        currentProgress = currentProgress.copy(
            directionsRoute = longerRoute
        )
        val response = obtainADirectionsResponse()

        val isFasterRoute = fasterRouteEngine.isFasterRoute(response, currentProgress)

        assertTrue(isFasterRoute)
    }

    @Test
    @Throws(Exception::class)
    fun onSlowerRouteResponse_isFasterRouteIsFalse() {
        val navigation = buildNavigationWithFasterRouteEnabled()
        val fasterRouteEngine = navigation.fasterRouteEngine
        var currentProgress = obtainDefaultRouteProgress()
        val longerRoute: DirectionsRoute = currentProgress.directionsRoute.copy(
            duration = 1000.0
        )
        currentProgress = currentProgress.copy(
            directionsRoute = longerRoute
        )
        val response = obtainADirectionsResponse()

        val isFasterRoute = fasterRouteEngine.isFasterRoute(response, currentProgress)

        assertFalse(isFasterRoute)
    }

    private fun buildNavigationWithFasterRouteEnabled(): TrackAsiaNavigation {
        val options = TrackAsiaNavigationOptions(enableFasterRouteDetection = true)
        return TrackAsiaNavigation(options = options, locationEngine = mockk())
    }

    @Throws(Exception::class)
    private fun obtainDefaultRouteProgress(): RouteProgress {
        val aRoute = obtainADirectionsRoute()
        return buildTestRouteProgress(aRoute, 100.0, 700.0, 1000.0, 0, 0)
    }

    @Throws(IOException::class)
    private fun obtainADirectionsRoute(): DirectionsRoute {
        val body = loadJsonFixture(PRECISION_6)
        val response = DirectionsResponse.fromJson(body)
        val aRoute = response.routes[0]

        return aRoute
    }

    @Throws(IOException::class)
    private fun obtainADirectionsResponse(): DirectionsResponse {
        val body = loadJsonFixture(PRECISION_6)
        val response = DirectionsResponse.fromJson(body)
        return response
    }

    companion object {
        private const val PRECISION_6 = "directions_v5_precision_6.json"
    }
}
