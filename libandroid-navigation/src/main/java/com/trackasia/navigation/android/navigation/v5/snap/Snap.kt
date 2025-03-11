package com.trackasia.navigation.android.navigation.v5.snap

import android.location.Location
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress
import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigation

/**
 * This class handles calculating snapped position along the route. Latitude, longitude and bearing
 * should be provided.
 *
 * The [TrackAsiaNavigation] uses
 * a [SnapToRoute] by default. If you would ike to customize the camera position, create a concrete implementation of this class
 * or subclass [SnapToRoute] and set it on [TrackAsiaNavigation] constructor}.
 */
abstract class Snap {

    /**
     * Calculate a snapped location along the route. Latitude, longitude and bearing should be
     * provided.
     *
     * @param location Current raw user location
     * @param routeProgress Current route progress
     * @return Snapped location along route
     */
    abstract fun getSnappedLocation(location: Location, routeProgress: RouteProgress): Location
}
