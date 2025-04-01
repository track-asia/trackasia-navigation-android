package com.trackasia.navigation.core.navigation

import com.trackasia.navigation.core.location.engine.AppleLocationEngine
import com.trackasia.navigation.core.location.engine.LocationEngine
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions.Defaults
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions.RoundingIncrement
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions.TimeFormat
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
 * A iOS platform specific wrapper for [TrackAsiaNavigation].
 *
 * You can also use [TrackAsiaNavigation] directly, but this leads to more configuration.
 *
 * Currently the only difference is, that the location engine is set to the [AppleLocationEngine]
 * by default.
 */
class IOSTrackAsiaNavigation(
    options: TrackAsiaNavigationOptions = TrackAsiaNavigationOptions(),
    locationEngine: LocationEngine = AppleLocationEngine(),
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
) {

    fun toBuilder(): Builder {
        return Builder()
            .withOptions(options)
            .withLocationEngine(locationEngine)
            .withCameraEngine(cameraEngine)
            .withSnapEngine(snapEngine)
            .withOffRouteEngine(offRouteEngine)
            .withFasterRouteEngine(fasterRouteEngine)
            .withRouteUtils(routeUtils)
    }

    class Builder {
        private var options: TrackAsiaNavigationOptions = TrackAsiaNavigationOptions()
        private var locationEngine: LocationEngine = AppleLocationEngine()
        private var cameraEngine: Camera = SimpleCamera()
        private var snapEngine: Snap = SnapToRoute()
        private var offRouteEngine: OffRoute = OffRouteDetector()
        private var fasterRouteEngine: FasterRoute = FasterRouteDetector(options)
        private var routeUtils: RouteUtils = RouteUtils()

        fun withOptions(options: TrackAsiaNavigationOptions) = apply { this.options = options }
        fun withLocationEngine(locationEngine: LocationEngine) =
            apply { this.locationEngine = locationEngine }

        fun withCameraEngine(cameraEngine: Camera) = apply { this.cameraEngine = cameraEngine }
        fun withSnapEngine(snapEngine: Snap) = apply { this.snapEngine = snapEngine }
        fun withOffRouteEngine(offRouteEngine: OffRoute) =
            apply { this.offRouteEngine = offRouteEngine }

        fun withFasterRouteEngine(fasterRouteEngine: FasterRoute) =
            apply { this.fasterRouteEngine = fasterRouteEngine }

        fun withRouteUtils(routeUtils: RouteUtils) = apply { this.routeUtils = routeUtils }

        fun build(): IOSTrackAsiaNavigation {
            return IOSTrackAsiaNavigation(
                options = options,
                locationEngine = locationEngine,
                cameraEngine = cameraEngine,
                snapEngine = snapEngine,
                offRouteEngine = offRouteEngine,
                fasterRouteEngine = fasterRouteEngine,
                routeUtils = routeUtils
            )
        }
    }
}
