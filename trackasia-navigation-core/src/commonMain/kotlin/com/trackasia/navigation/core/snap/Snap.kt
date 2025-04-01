package com.trackasia.navigation.core.snap

import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation

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
