package com.trackasia.navigation.core.navigation

import co.touchlab.kermit.Logger
import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.navigation.core.route.FasterRoute
import com.trackasia.navigation.core.route.RouteListener
import com.trackasia.navigation.core.routeprogress.RouteProgress

open class NavigationFasterRouteListener(
    private val eventDispatcher: NavigationEventDispatcher,
    private val fasterRouteEngine: FasterRoute
) : RouteListener {

    override fun onResponseReceived(response: DirectionsResponse, routeProgress: RouteProgress) {
        if (fasterRouteEngine.isFasterRoute(response, routeProgress)) {
            eventDispatcher.onFasterRouteEvent(response.routes.first())
        }
    }

    override fun onErrorReceived(throwable: Throwable) {
        Logger.e(throwable) { "Error occurred fetching a faster route" }
    }
}
