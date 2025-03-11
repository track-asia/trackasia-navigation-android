package com.trackasia.navigation.android.navigation.v5.utils.time

import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigationOptions
import java.util.Calendar


interface TimeFormatResolver {
    fun nextChain(chain: TimeFormatResolver?)

    fun obtainTimeFormatted(type: TrackAsiaNavigationOptions.TimeFormat, time: Calendar): String?
}