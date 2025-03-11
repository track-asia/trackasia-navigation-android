package com.trackasia.navigation.android.navigation.v5.navigation

import android.content.Context
import com.trackasia.navigation.android.navigation.v5.navigation.notification.NavigationNotification
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress

open class NavigationNotificationProvider(
    context: Context,
    trackAsiaNavigation: TrackAsiaNavigation
) {
    private val navigationNotification: NavigationNotification =
        buildNotificationFrom(context, trackAsiaNavigation)
    private var shouldUpdate = true

    fun retrieveNotification(): NavigationNotification {
        return navigationNotification
    }

    fun updateNavigationNotification(routeProgress: RouteProgress) {
        if (shouldUpdate) {
            navigationNotification.updateNotification(routeProgress)
        }
    }

    fun shutdown(context: Context) {
        navigationNotification.onNavigationStopped(context)
        shouldUpdate = false
    }

    private fun buildNotificationFrom(
        context: Context,
        trackAsiaNavigation: TrackAsiaNavigation
    ): NavigationNotification {
        return trackAsiaNavigation.options.navigationNotification
            ?: TrackAsiaNavigationNotification(
                context,
                trackAsiaNavigation
            )
    }
}
