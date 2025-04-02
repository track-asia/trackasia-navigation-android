package com.trackasia.navigation.android.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.trackasia.geojson.Point
import com.trackasia.android.annotations.MarkerOptions
import com.trackasia.android.camera.CameraPosition
import com.trackasia.android.geometry.LatLng
import com.trackasia.android.location.LocationComponentActivationOptions
import com.trackasia.android.location.modes.CameraMode
import com.trackasia.android.location.modes.RenderMode
import com.trackasia.android.maps.TrackAsiaMap
import com.trackasia.android.maps.OnMapReadyCallback
import com.trackasia.android.maps.Style
import com.trackasia.navigation.android.example.databinding.ActivityNavigationUiBinding
import com.trackasia.navigation.android.navigation.ui.v5.NavigationLauncher
import com.trackasia.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import com.trackasia.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.models.RouteOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import kotlin.math.*

/**
 * Navigation activity specifically designed to work with the TrackAsia routing service
 * at maps.track-asia.com
 */
class TrackAsiaNavigationActivity : AppCompatActivity(), OnMapReadyCallback, TrackAsiaMap.OnMapClickListener {

    private lateinit var trackAsiaMap: TrackAsiaMap
    private lateinit var binding: ActivityNavigationUiBinding

    private var language = Locale.getDefault().language
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var simulateRoute = false

    private val apiKey = "public_key" // Replace with your API key
    private val client = OkHttpClient()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup logging
        Timber.plant(Timber.DebugTree())
        
        // Log device info at startup
        Timber.d("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        Timber.d("Android version: ${android.os.Build.VERSION.RELEASE}")
        
        binding = ActivityNavigationUiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        title = "Track-Asia Điều Hướng"
        setupUI()
        
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@TrackAsiaNavigationActivity)
        }
    }

    private fun setupUI() {
        binding.startRouteButton.apply { 
            text = "Bắt Đầu Điều Hướng"
            setOnClickListener {
                route?.let { route ->
                    try {
                        trackAsiaMap.locationComponent.lastKnownLocation?.let { location ->
                            startNavigation(route, location)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error starting navigation")
                        showToast("Lỗi khi bắt đầu điều hướng: ${e.message}")
                    }
                }
            }
        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            simulateRoute = checked
        }

        binding.clearPoints.setOnClickListener { clearMapAndRoute() }
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

    override fun onMapReady(map: TrackAsiaMap) {
        trackAsiaMap = map
        map.setStyle(Style.Builder().fromUri(getString(R.string.map_style_light))) { style ->
            enableLocationComponent(style)
            navigationMapRoute = NavigationMapRoute(binding.mapView, map)
            map.addOnMapClickListener(this)
            
            Snackbar.make(findViewById(R.id.container), "Tap map to place destination", Snackbar.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        trackAsiaMap.locationComponent.apply {
            activateLocationComponent(
                LocationComponentActivationOptions.builder(this@TrackAsiaNavigationActivity, style).build()
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
        val userLocation = trackAsiaMap.locationComponent.lastKnownLocation ?: return
        val destination = destination ?: return

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        
        // Check if distance is less than 50m
        val distanceInMeters = calculateDistance(
            origin.latitude(), origin.longitude(),
            destination.latitude(), destination.longitude()
        )
        
        if (distanceInMeters < 50) {
            Timber.d("calculateRoute: distance < 50 m")
            binding.startRouteButton.visibility = View.GONE
            return
        }

        requestRoute(origin, destination)
    }

    // Simple distance calculation using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth's radius in meters
        val φ1 = lat1.toRadians()
        val φ2 = lat2.toRadians()
        val Δφ = (lat2 - lat1).toRadians()
        val Δλ = (lon2 - lon1).toRadians()

        val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun Double.toRadians() = this * PI / 180

    private fun requestRoute(origin: Point, destination: Point) {
        val baseUrl = "https://maps.track-asia.com/route/v1/car"
        val originCoord = "${origin.longitude()},${origin.latitude()}"
        val destCoord = "${destination.longitude()},${destination.latitude()}"
        val url = "$baseUrl/$originCoord;$destCoord.json?geometries=polyline6&steps=true&overview=full&key=$apiKey"

        val request = Request.Builder()
            .header("User-Agent", "TrackAsia Android Navigation SDK Demo App")
            .url(url)
            .build()

        Timber.d("Requesting route: %s", url)

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Timber.e(e, "Failed to get route from TrackAsia Routing")
                showToast("Không thể kết nối tới dịch vụ định tuyến")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Timber.e("Request failed with code: %s", response.code)
                        showToast("Yêu cầu thất bại với mã: ${response.code}")
                        return
                    }

                    response.body?.string()?.let { json ->
                        processRouteResponse(json, origin, destination)
                    }
                }
            }
        })
    }

    private fun processRouteResponse(responseJson: String, origin: Point, destination: Point) {
        try {
            val response = DirectionsResponse.fromJson(responseJson)
            if (response.routes.isEmpty()) {
                showToast("Không tìm thấy tuyến đường nào")
                return
            }
            
            val firstRoute = response.routes.first()
            if (firstRoute.geometry.isEmpty()) {
                showToast("Geometry của tuyến đường trống")
                return
            }
            
            // Create route with RouteOptions
            route = firstRoute.copy(
                routeOptions = RouteOptions(
                    baseUrl = "https://maps.track-asia.com/route/v1",
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

            displayRouteOnMap(response.routes)
        } catch (e: Exception) {
            Timber.e(e, "Error processing API response")
            showToast("Lỗi xử lý dữ liệu: ${e.message}")
        }
    }

    private fun displayRouteOnMap(routes: List<DirectionsRoute>) {
        runOnUiThread {
            try {
                if (navigationMapRoute == null) {
                    navigationMapRoute = NavigationMapRoute(binding.mapView, trackAsiaMap)
                }
                navigationMapRoute?.addRoutes(routes)
                binding.startRouteLayout.visibility = View.VISIBLE
            } catch (e: Exception) {
                Timber.e(e, "Error displaying route")
                showToast("Lỗi hiển thị route: ${e.message}")
            }
        }
    }

    private fun startNavigation(route: DirectionsRoute, userLocation: android.location.Location) {
        try {
            val options = NavigationLauncherOptions.builder()
                .directionsRoute(route)
                .shouldSimulateRoute(simulateRoute)
                .initialMapCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(userLocation.latitude, userLocation.longitude))
                        .build()
                )
                .lightThemeResId(R.style.TestNavigationViewLight)
                .darkThemeResId(R.style.TestNavigationViewDark)
                .build()
            
            NavigationLauncher.startNavigation(this, options)
        } catch (e: Exception) {
            Timber.e(e, "Navigation failed to start: ${e.message}")
            showToast("Lỗi khi khởi động navigation: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    // Lifecycle methods
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