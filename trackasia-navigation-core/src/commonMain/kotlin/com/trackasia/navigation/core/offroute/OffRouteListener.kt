package com.trackasia.navigation.core.offroute

import com.trackasia.navigation.core.location.Location

fun interface OffRouteListener {
    fun userOffRoute(location: Location)
}
