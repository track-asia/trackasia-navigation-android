package com.trackasia.navigation.android.navigation.v5.utils.time

import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigationOptions
import java.util.Calendar
import java.util.Locale


internal class TwelveHoursTimeFormat : TimeFormatResolver {
    private var chain: TimeFormatResolver? = null

    override fun nextChain(chain: TimeFormatResolver?) {
        this.chain = chain
    }

    override fun obtainTimeFormatted(
        type: TrackAsiaNavigationOptions.TimeFormat,
        time: Calendar
    ): String? {
        return if (type == TrackAsiaNavigationOptions.TimeFormat.TWELVE_HOURS) {
            String.format(
                Locale.getDefault(),
                TWELVE_HOURS_FORMAT,
                time,
                time,
                time
            )
        } else {
            chain?.obtainTimeFormatted(type, time)
        }
    }

    companion object {
        const val TWELVE_HOURS_FORMAT: String = "%tl:%tM %tp"
    }
}
