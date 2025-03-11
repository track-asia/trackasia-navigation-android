package com.trackasia.navigation.android.navigation.v5.offroute

import android.location.Location
import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigationOptions
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress

fun interface OffRoute {

    fun isUserOffRoute(location: Location, routeProgress: RouteProgress, options: TrackAsiaNavigationOptions): Boolean
}
