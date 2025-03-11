package com.trackasia.navigation.android.example;

import android.app.Application;

import com.trackasia.android.BuildConfig;
import com.trackasia.android.TrackAsia;

import timber.log.Timber;

public class NavigationApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    TrackAsia.getInstance(getApplicationContext());
  }

}
