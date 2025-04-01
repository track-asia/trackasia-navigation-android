package com.trackasia.navigation.core.routeprogress

import com.trackasia.navigation.core.location.Location

fun interface ProgressChangeListener {
    fun onProgressChange(location: Location, routeProgress: RouteProgress)
}
