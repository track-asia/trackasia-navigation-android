package com.trackasia.navigation.android.navigation.v5.navigation

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import com.trackasia.navigation.android.navigation.v5.navigation.notification.NavigationNotification
import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress

class NavigationNotificationProviderTest {

    @Test
    fun updateNavigationNotification() {
        val notification = mockk<NavigationNotification>(relaxed = true)
        val trackAsiaNavigation = buildNavigationWithNotificationOptions(notification)
        val context = mockk<Context>()
        val provider = NavigationNotificationProvider(context, trackAsiaNavigation)

        val routeProgress = mockk<RouteProgress>()
        provider.updateNavigationNotification(routeProgress)

        verify {
            notification.updateNotification(routeProgress)
        }
    }

    @Test
    fun updateNavigationNotification_doesNotUpdateAfterShutdown() {
        val notification = mockk<NavigationNotification>(relaxed = true)
        val trackAsiaNavigation = buildNavigationWithNotificationOptions(notification)
        val context = mockk<Context>()
        val provider = NavigationNotificationProvider(context, trackAsiaNavigation)
        val routeProgress = mockk<RouteProgress>()

        provider.shutdown(context)
        provider.updateNavigationNotification(routeProgress)

        verify(exactly = 0) {
            notification.updateNotification(routeProgress)
        }
    }

    @Test
    fun onShutdown_onNavigationStoppedIsCalled() {
        val notification = mockk<NavigationNotification>(relaxed = true)
        val trackAsiaNavigation = buildNavigationWithNotificationOptions(notification)
        val context = mockk<Context>()
        val provider = NavigationNotificationProvider(context, trackAsiaNavigation)

        provider.shutdown(context)

        verify {
            notification.onNavigationStopped(context)
        }
    }

    private fun buildNavigationWithNotificationOptions(notification: NavigationNotification): TrackAsiaNavigation {
        return mockk<TrackAsiaNavigation> {
            every { options } returns mockk<TrackAsiaNavigationOptions> {
                every { navigationNotification } returns notification
            }
        }
    }
}