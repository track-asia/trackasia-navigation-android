package com.trackasia.navigation.android.navigation.ui.v5

import com.trackasia.geojson.common.toJvm
import com.trackasia.geojson.model.Point
import com.trackasia.geojson.Point as JvmPoint

fun List<Point>.toJvmPoints(): List<JvmPoint> = map { pt -> pt.toJvm() }
