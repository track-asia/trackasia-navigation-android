package com.trackasia.navigation.android.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.trackasia.geojson.Point
import com.trackasia.android.annotations.MarkerOptions
import com.trackasia.android.camera.CameraPosition
import com.trackasia.android.geometry.LatLng
import com.trackasia.android.location.LocationComponent
import com.trackasia.android.location.LocationComponentActivationOptions
import com.trackasia.android.location.modes.CameraMode
import com.trackasia.android.location.modes.RenderMode
import com.trackasia.android.maps.TrackAsiaMap
import com.trackasia.android.maps.OnMapReadyCallback
import com.trackasia.android.maps.Style
import com.trackasia.navigation.android.example.databinding.ActivityNavigationUiBinding
import com.trackasia.navigation.android.navigation.ui.v5.NavigationLauncher
import com.trackasia.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.models.RouteOptions
import com.trackasia.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import com.trackasia.turf.TurfConstants
import com.trackasia.turf.TurfMeasurement
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.util.Locale

/**
 * Navigation activity specifically designed to work with the TrackAsia routing service
 * at maps.track-asia.com
 */
class TrackAsiaNavigationActivity : AppCompatActivity(), OnMapReadyCallback,
    TrackAsiaMap.OnMapClickListener {

    private lateinit var trackAsiaMap: TrackAsiaMap
    private lateinit var binding: ActivityNavigationUiBinding

    private var language = Locale.getDefault().language
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var locationComponent: LocationComponent? = null
    private var simulateRoute = false

    private val apiKey = "public_key" // Replace with your API key
    private val client = OkHttpClient()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityNavigationUiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@TrackAsiaNavigationActivity)
        }

        title = "Track-Asia Điều Hướng"
        setupUI()
    }

    private fun setupUI() {
        binding.startRouteButton.text = "Bắt Đầu Điều Hướng"
        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                try {
                    val userLocation =
                        trackAsiaMap.locationComponent.lastKnownLocation ?: return@let
                    startNavigation(route, userLocation)
                } catch (e: Exception) {
                    Timber.e(e, "Error starting navigation")
                    showErrorMessage("Lỗi khi bắt đầu điều hướng: ${e.message}")
                }
            }
        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            simulateRoute = checked
        }

        binding.clearPoints.setOnClickListener {
            clearMapAndRoute()
        }
    }

    private fun clearMapAndRoute() {
        if (::trackAsiaMap.isInitialized) {
            trackAsiaMap.markers.forEach { trackAsiaMap.removeMarker(it) }
        }
        destination = null
        binding.clearPoints.visibility = View.GONE
        binding.startRouteLayout.visibility = View.GONE
        navigationMapRoute?.removeRoute()
    }

    override fun onMapReady(trackAsiaMap: TrackAsiaMap) {
        this.trackAsiaMap = trackAsiaMap
        trackAsiaMap.setStyle(
            Style.Builder().fromUri(getString(R.string.map_style_light))
        ) { style ->
            enableLocationComponent(style)
            navigationMapRoute = NavigationMapRoute(binding.mapView, trackAsiaMap)
            trackAsiaMap.addOnMapClickListener(this)

            Snackbar.make(
                findViewById(R.id.container),
                "Tap map to place destination",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        locationComponent = trackAsiaMap.locationComponent.apply {
            activateLocationComponent(
                LocationComponentActivationOptions.builder(this@TrackAsiaNavigationActivity, style)
                    .build()
            )
            isLocationComponentEnabled = true
            cameraMode = CameraMode.TRACKING_GPS_NORTH
            renderMode = RenderMode.NORMAL
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        destination = Point.fromLngLat(point.longitude, point.latitude)
        trackAsiaMap.addMarker(MarkerOptions().position(point))
        binding.clearPoints.visibility = View.VISIBLE
        calculateRoute()
        return true
    }

    private fun calculateRoute() {
        binding.startRouteLayout.visibility = View.GONE
        val userLocation = trackAsiaMap.locationComponent.lastKnownLocation
        val destination = destination

        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null")
            return
        }

        if (destination == null) {
            Timber.d("calculateRoute: Destination is null")
            return
        }

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            Timber.d("calculateRoute: distance < 50 m")
            binding.startRouteButton.visibility = View.GONE
            return
        }

        requestRoute(origin, destination)
    }

    private fun requestRoute(origin: Point, destination: Point) {
        val baseUrl = "https://maps.track-asia.com/route/v1/car"
        val originCoord = "${origin.longitude()},${origin.latitude()}"
        val destCoord = "${destination.longitude()},${destination.latitude()}"
        val url =
            "$baseUrl/$originCoord;$destCoord.json?geometries=polyline6&steps=true&overview=full&key=$apiKey"

        val request = Request.Builder()
            .header("User-Agent", "TrackAsia Android Navigation SDK Demo App")
            .url(url)
            .build()

        Timber.d("Requesting route: %s", url)

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e(e, "Failed to get route from TrackAsia Routing")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Timber.e("Request failed with code: %s", response.code)
                        return
                    }

                    val responseBodyJson = response.body!!.string()
                    Timber.d("Response JSON: %s", responseBodyJson)

                    processRouteResponse(responseBodyJson, origin, destination)
                }
            }
        })
    }

    private fun processRouteResponse(responseBodyJson: String, origin: Point, destination: Point) {
        try {
            val trackasiaResponse = DirectionsResponse.fromJson(responseBodyJson)
            if (trackasiaResponse.routes.isEmpty()) {
                showErrorMessage("Không tìm thấy tuyến đường nào")
                return
            }
            val firstRoute = trackasiaResponse.routes.first()
            Timber.d("Route: distance=${firstRoute.distance}m, duration=${firstRoute.duration}s")

            val routeGeometry = firstRoute.geometry
            if (routeGeometry.isEmpty()) {
                showErrorMessage("Geometry của tuyến đường trống")
                return
            }
            // Create route with RouteOptions
            this@TrackAsiaNavigationActivity.route = firstRoute.copy(
                routeOptions = RouteOptions(
                    baseUrl = "https://maps.track-asia.com/route/v1/car",
                    profile = "car",
                    user = "trackasia",
                    accessToken = apiKey,
                    voiceInstructions = true,
                    bannerInstructions = true,
                    language = language,
                    coordinates = listOf(origin, destination),
                    geometries = "polyline6",
                    steps = true,
                    overview = "full",
                    requestUuid = "trackasia-nav-${System.currentTimeMillis()}"
                )
            )

            displayRouteOnMap(trackasiaResponse.routes)
        } catch (e: Exception) {
            Timber.e(e, "Error processing API response")
            showErrorMessage("Lỗi xử lý dữ liệu: ${e.message}")
        }
    }

    private fun displayRouteOnMap(routes: List<DirectionsRoute>) {
        runOnUiThread {
            try {
                Timber.d("Adding ${routes.size} routes to map")
                if (navigationMapRoute == null) {
                    navigationMapRoute = NavigationMapRoute(binding.mapView, trackAsiaMap)
                }
                navigationMapRoute?.addRoutes(routes)
                binding.startRouteLayout.visibility = View.VISIBLE
            } catch (e: Exception) {
                Timber.e(e, "Error displaying route")
                showErrorMessage("Lỗi hiển thị route: ${e.message}")
            }
        }
    }

    private fun showErrorMessage(message: String) {
        runOnUiThread {
            Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startNavigation(route: DirectionsRoute, userLocation: android.location.Location) {
        try {
            // Kiểm tra lại route để đảm bảo an toàn
            Timber.d("Starting navigation with fixed core - Route: ${route.distance}m, ${route.duration}s")
            
            val options = NavigationLauncherOptions.builder()
                .directionsRoute(route)
                .shouldSimulateRoute(simulateRoute)
                .initialMapCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(userLocation.latitude, userLocation.longitude)).build()
                )
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            
            NavigationLauncher.startNavigation(this@TrackAsiaNavigationActivity, options)
        } catch (e: Exception) {
            Timber.e(e, "Navigation failed to start")
            showErrorMessage("Không thể bắt đầu điều hướng: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::trackAsiaMap.isInitialized) {
            trackAsiaMap.removeOnMapClickListener(this)
        }
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
} 