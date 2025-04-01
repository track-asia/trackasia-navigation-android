package com.trackasia.navigation.core.offroute

import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions
import com.trackasia.navigation.core.routeprogress.RouteProgress

fun interface OffRoute {

    fun isUserOffRoute(location: Location, routeProgress: RouteProgress, options: TrackAsiaNavigationOptions): Boolean
}
