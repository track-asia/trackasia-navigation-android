package com.trackasia.navigation.android.example

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.trackasia.navigation.core.models.DirectionsResponse
import com.trackasia.android.annotations.MarkerOptions
import com.trackasia.android.camera.CameraUpdateFactory
import com.trackasia.android.geometry.LatLng
import com.trackasia.android.location.LocationComponent
import com.trackasia.android.location.LocationComponentActivationOptions
import com.trackasia.android.location.modes.CameraMode
import com.trackasia.android.location.modes.RenderMode
import com.trackasia.android.maps.TrackAsiaMap
import com.trackasia.android.maps.OnMapReadyCallback
import com.trackasia.android.maps.Style
import com.trackasia.navigation.android.navigation.ui.v5.route.NavigationRoute
import com.trackasia.navigation.core.location.replay.ReplayRouteLocationEngine
import com.trackasia.navigation.core.models.DirectionsRoute
import com.trackasia.navigation.core.offroute.OffRouteListener
import com.trackasia.navigation.core.routeprogress.ProgressChangeListener
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.turf.TurfConstants
import com.trackasia.turf.TurfMeasurement
import okhttp3.Request
import com.trackasia.navigation.android.example.databinding.ActivityMockNavigationBinding
import com.trackasia.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import com.trackasia.navigation.core.instruction.Instruction
import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.milestone.Milestone
import com.trackasia.navigation.core.milestone.MilestoneEventListener
import com.trackasia.navigation.core.milestone.RouteMilestone
import com.trackasia.navigation.core.milestone.Trigger
import com.trackasia.navigation.core.milestone.TriggerProperty
import com.trackasia.navigation.core.models.UnitType
import com.trackasia.navigation.core.navigation.AndroidTrackAsiaNavigation
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation
import com.trackasia.navigation.core.navigation.NavigationEventListener
import com.trackasia.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.ref.WeakReference

class MockNavigationActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    TrackAsiaMap.OnMapClickListener,
    ProgressChangeListener,
    NavigationEventListener,
    MilestoneEventListener,
    OffRouteListener {
    private val BEGIN_ROUTE_MILESTONE = 1001
    private lateinit var trackAsiaMap: TrackAsiaMap

    // Navigation related variables
    private var locationEngine: ReplayRouteLocationEngine =
        ReplayRouteLocationEngine()
    private lateinit var navigation: TrackAsiaNavigation
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var waypoint: Point? = null
    private var locationComponent: LocationComponent? = null

    private lateinit var binding: ActivityMockNavigationBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MockNavigationActivity)
        }

        navigation = AndroidTrackAsiaNavigation(applicationContext)
        navigation.addMilestone(
            RouteMilestone(
                identifier = BEGIN_ROUTE_MILESTONE,
                instruction = BeginRouteInstruction(),
                trigger = Trigger.all(
                    Trigger.lt(
                        TriggerProperty.STEP_INDEX, 3
                    ),
                    Trigger.gt(
                        TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 200
                    ),
                    Trigger.gte(
                        TriggerProperty.STEP_DISTANCE_TRAVELED_METERS, 75
                    ),
                ),
            )
        )

        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                binding.startRouteButton.visibility = View.INVISIBLE

                // Attach all of our navigation listeners.
                navigation.apply {
                    addNavigationEventListener(this@MockNavigationActivity)
                    addProgressChangeListener(this@MockNavigationActivity)
                    addMilestoneEventListener(this@MockNavigationActivity)
                    addOffRouteListener(this@MockNavigationActivity)
                }

                locationEngine.also {
                    it.assign(route)
                    navigation.locationEngine = it
                    navigation.startNavigation(route)
                    if (::trackAsiaMap.isInitialized) {
                        trackAsiaMap.removeOnMapClickListener(this)
                    }
                }
            }
        }

        binding.newLocationFab.setOnClickListener {
            newOrigin()
        }

        binding.clearPoints.setOnClickListener {
            if (::trackAsiaMap.isInitialized) {
                trackAsiaMap.markers.forEach {
                    trackAsiaMap.removeMarker(it)
                }
            }
            destination = null
            waypoint = null
            it.visibility = View.GONE

            navigationMapRoute?.removeRoute()
        }
    }

    override fun onMapReady(trackAsiaMap: TrackAsiaMap) {
        this.trackAsiaMap = trackAsiaMap
        trackAsiaMap.setStyle(
            Style.Builder().fromUri(getString(R.string.map_style_light))
        ) { style ->
            enableLocationComponent(style)
            navigationMapRoute = NavigationMapRoute(navigation, binding.mapView, trackAsiaMap)

            trackAsiaMap.addOnMapClickListener(this)
            Snackbar.make(
                findViewById(R.id.container),
                "Tap map to place waypoint",
                Snackbar.LENGTH_LONG,
            ).show()

            newOrigin()
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
        locationComponent = trackAsiaMap.locationComponent

        locationComponent?.let {
            // Activate with a built LocationComponentActivationOptions object
            it.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build(),
            )

            // Enable to make component visible
            it.isLocationComponentEnabled = true

            // Set the component's camera mode
            it.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            it.renderMode = RenderMode.GPS

//            it.locationEngine = locationEngine
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        var addMarker = true
        when {
            destination == null -> destination = Point.fromLngLat(point.longitude, point.latitude, point.altitude)
            waypoint == null -> waypoint = Point.fromLngLat(point.longitude, point.latitude, point.altitude)
            else -> {
                Toast.makeText(this, "Only 2 waypoints supported", Toast.LENGTH_LONG).show()
                addMarker = false
            }
        }

        if (addMarker) {
            trackAsiaMap.addMarker(MarkerOptions().position(point))
        }
        binding.clearPoints.visibility = View.VISIBLE

        binding.startRouteButton.visibility = View.VISIBLE
        calculateRoute()
        return true
    }

    private fun calculateRoute() {
        val userLocation = locationEngine.lastLocation
        val destination = destination
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
            return
        }

        if (destination == null) {
            return
        }

        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude, userLocation.altitude ?: 0.0)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            binding.startRouteButton.visibility = View.GONE
            return
        }

        val navigationRouteBuilder = NavigationRoute.builder(this).apply {
            this.accessToken(getString(R.string.mapbox_access_token))
            this.origin(origin)
            this.destination(destination)
            this.voiceUnits(UnitType.METRIC)
            this.alternatives(true)
            this.baseUrl(getString(R.string.base_url))
        }

        navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>,
            ) {
                Timber.d("Url: %s", (call.request() as Request).url.toString())
                response.body()?.let { response ->
                    if (response.routes.isNotEmpty()) {
                        val trackasiaResponse = DirectionsResponse.fromJson(response.toJson());
                        val directionsRoute = trackasiaResponse.routes.first()
                        this@MockNavigationActivity.route = directionsRoute
                        navigationMapRoute?.addRoutes(trackasiaResponse.routes)
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()")
            }
        })
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
    }

    override fun onRunning(running: Boolean) {
    }

    override fun onMilestoneEvent(
        routeProgress: RouteProgress,
        instruction: String?,
        milestone: Milestone,
    ) {
    }

    override fun userOffRoute(location: Location) {
    }

    private class BeginRouteInstruction : Instruction {

        override fun buildInstruction(routeProgress: RouteProgress): String {
            return "Have a safe trip!"
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
        navigation.onDestroy()
        if (::trackAsiaMap.isInitialized) {
            trackAsiaMap.removeOnMapClickListener(this)
        }
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private class MyBroadcastReceiver internal constructor(navigation: TrackAsiaNavigation) :
        BroadcastReceiver() {
        private val weakNavigation: WeakReference<TrackAsiaNavigation> = WeakReference(navigation)

        override fun onReceive(context: Context, intent: Intent) {
            weakNavigation.get()?.stopNavigation()
        }
    }

    private fun newOrigin() {
        trackAsiaMap.let {
            val latLng = LatLng(52.039176, 5.550339)
            locationEngine.assignLastLocation(
                Point.fromLngLat(latLng.longitude, latLng.latitude),
            )
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0))
        }
    }
}
