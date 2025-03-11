package com.trackasia.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable
import com.trackasia.geojson.Point
import com.trackasia.navigation.android.navigation.v5.models.serializer.PointSerializer

/**
 * An input coordinate snapped to the roads network.
 *
 * @since 1.0.0
 */
@Serializable
data class DirectionsWaypoint(

    /**
     * Provides the way name which the waypoint's coordinate is snapped to.
     *
     * @since 1.0.0
     */
    val name: String?,

    /**
     * A [Point] representing this waypoint location.
     *
     * @since 3.0.0
     */
    @Serializable(with = PointSerializer::class)
    val location: Point?
)
