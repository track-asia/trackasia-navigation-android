package com.trackasia.navigation.core.route

import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.navigation.core.routeprogress.RouteProgress

/**
 * Will fire when either a successful / failed response is received.
 */
interface RouteListener {
    fun onResponseReceived(response: DirectionsResponse, routeProgress: RouteProgress)

    fun onErrorReceived(throwable: Throwable)
}
