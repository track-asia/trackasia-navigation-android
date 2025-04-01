package com.trackasia.navigation.core.navigation

import com.trackasia.navigation.core.location.Location

data class NavigationLocationUpdate(
    val location: Location,
    val trackAsiaNavigation: TrackAsiaNavigation
)
