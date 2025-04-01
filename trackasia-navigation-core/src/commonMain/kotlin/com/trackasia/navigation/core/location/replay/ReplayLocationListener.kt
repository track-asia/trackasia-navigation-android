package com.trackasia.navigation.core.location.replay

import com.trackasia.navigation.core.location.Location

fun interface ReplayLocationListener {
    fun onLocationReplay(location: Location)
}
