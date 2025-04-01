package com.trackasia.navigation.core.location.engine

import android.content.Context
import android.location.LocationListener
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.trackasia.android.location.engine.LocationEngineCallback
import com.trackasia.android.location.engine.LocationEngineRequest as TrackAsiaLocationRequest
import com.trackasia.android.location.engine.LocationEngineResult
import com.trackasia.android.location.engine.TrackAsiaFusedLocationEngineImpl
import com.trackasia.navigation.core.location.Location
import com.trackasia.navigation.core.location.toLocation
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Location engine, that using the default TrackAsiaLocation engine.
 *
 * @param context used to initialize the underlying [TrackAsiaFusedLocationEngineImpl]
 * @param looper looper that is ued by the [TrackAsiaFusedLocationEngineImpl] to listen on for location updates
 */
open class TrackAsiaLocationEngine(
    context: Context,
    private val looper: Looper?
) : LocationEngine {

    /**
     * Underlying [TrackAsiaFusedLocationEngineImpl] that is used to fetch location and listen to location updates.
     */
    private val trackasiaLocationEngine = TrackAsiaFusedLocationEngineImpl(context)

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toLocation()) }

        trackasiaLocationEngine.requestLocationUpdates(
            toTrackAsiaLocationRequest(request),
            listener,
            looper,
        )

        awaitClose { trackasiaLocationEngine.removeLocationUpdates(listener) }
    }

    override suspend fun getLastLocation(): Location? = suspendCoroutine { continuation ->
        trackasiaLocationEngine.getLastLocation(object :
            LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(locationEngineResult: LocationEngineResult) {
                continuation.resume(locationEngineResult.lastLocation?.toLocation())
            }

            override fun onFailure(exception: Exception) {
                continuation.resumeWithException(exception)
            }
        })
    }

    private fun toTrackAsiaLocationRequest(request: LocationEngine.Request): TrackAsiaLocationRequest {
        return TrackAsiaLocationRequest.Builder(request.maxIntervalMilliseconds)
            .setFastestInterval(request.minIntervalMilliseconds)
            .setDisplacement(request.minUpdateDistanceMeters)
            .setMaxWaitTime(request.maxUpdateDelayMilliseconds)
            .setPriority(toTrackAsiaPriority(request.accuracy))
            .build()
    }

    private fun toTrackAsiaPriority(accuracy: LocationEngine.Request.Accuracy): Int {
        return when (accuracy) {
            LocationEngine.Request.Accuracy.PASSIVE -> TrackAsiaLocationRequest.PRIORITY_NO_POWER
            LocationEngine.Request.Accuracy.LOW -> TrackAsiaLocationRequest.PRIORITY_LOW_POWER
            LocationEngine.Request.Accuracy.BALANCED -> TrackAsiaLocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            LocationEngine.Request.Accuracy.HIGH -> TrackAsiaLocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}
