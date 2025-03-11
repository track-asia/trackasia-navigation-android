package com.trackasia.navigation.android.navigation.v5.navigation

import android.location.Location

data class NavigationLocationUpdate(
    val location: Location,
    val trackAsiaNavigation: TrackAsiaNavigation
)
