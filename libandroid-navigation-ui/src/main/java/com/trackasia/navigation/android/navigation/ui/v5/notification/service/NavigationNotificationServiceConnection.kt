package com.trackasia.navigation.android.navigation.ui.v5.notification.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.trackasia.navigation.android.navigation.ui.v5.notification.TrackAsiaNavigationNotification
import com.trackasia.navigation.android.navigation.ui.v5.notification.NavigationNotification
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation


class NavigationNotificationServiceConnection(
    private val trackAsiaNavigation: TrackAsiaNavigation,
    private val navigationNotification: NavigationNotification,
) : ServiceConnection {

    constructor(
        context: Context,
        trackAsiaNavigation: TrackAsiaNavigation,
    ) : this(trackAsiaNavigation, TrackAsiaNavigationNotification(context, trackAsiaNavigation))

    private var serviceBinder: NavigationNotificationService.LocalBinder? = null

    fun start(context: Context) {
        val intent = Intent(context, NavigationNotificationService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun stop(context: Context) {
        context.unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        (service as NavigationNotificationService.LocalBinder).also { serviceBinder ->
            this.serviceBinder = serviceBinder

            serviceBinder.service.navigationNotification = navigationNotification
            trackAsiaNavigation.addNavigationEventListener(serviceBinder.service)
            trackAsiaNavigation.addProgressChangeListener(serviceBinder.service)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        serviceBinder?.let { serviceBinder ->
            trackAsiaNavigation.removeNavigationEventListener(serviceBinder.service)
            trackAsiaNavigation.removeProgressChangeListener(serviceBinder.service)
        }

        serviceBinder = null
    }
}