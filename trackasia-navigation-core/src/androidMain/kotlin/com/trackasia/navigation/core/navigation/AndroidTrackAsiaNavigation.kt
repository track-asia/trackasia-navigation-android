package com.trackasia.navigation.core.navigation

import android.content.Context
import com.trackasia.navigation.core.location.engine.LocationEngine
import com.trackasia.navigation.core.location.engine.LocationEngineProvider
import com.trackasia.navigation.core.navigation.camera.Camera
import com.trackasia.navigation.core.navigation.camera.SimpleCamera
import com.trackasia.navigation.core.offroute.OffRoute
import com.trackasia.navigation.core.offroute.OffRouteDetector
import com.trackasia.navigation.core.route.FasterRoute
import com.trackasia.navigation.core.route.FasterRouteDetector
import com.trackasia.navigation.core.snap.Snap
import com.trackasia.navigation.core.snap.SnapToRoute
import com.trackasia.navigation.core.utils.RouteUtils

/**
 * A Android platform specific wrapper for [TrackAsiaNavigation].
 *
 * You can also use [TrackAsiaNavigation] directly, but this leads to more configuration.
 *
 * Currently the only difference is, that the location engine is created depending on
 * your dependencies.
 */
class AndroidTrackAsiaNavigation(
    context: Context,
    options: TrackAsiaNavigationOptions = TrackAsiaNavigationOptions(),
    locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context),
    cameraEngine: Camera = SimpleCamera(),
    snapEngine: Snap = SnapToRoute(),
    offRouteEngine: OffRoute = OffRouteDetector(),
    fasterRouteEngine: FasterRoute = FasterRouteDetector(options),
    routeUtils: RouteUtils = RouteUtils(),
) : TrackAsiaNavigation(
    options = options,
    locationEngine = locationEngine,
    cameraEngine = cameraEngine,
    snapEngine = snapEngine,
    offRouteEngine = offRouteEngine,
    fasterRouteEngine = fasterRouteEngine,
    routeUtils = routeUtils
)
