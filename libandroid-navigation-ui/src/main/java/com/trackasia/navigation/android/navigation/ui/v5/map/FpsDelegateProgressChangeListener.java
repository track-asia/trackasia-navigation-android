package com.trackasia.navigation.android.navigation.ui.v5.map;

import com.trackasia.navigation.core.location.Location;

import com.trackasia.navigation.core.routeprogress.ProgressChangeListener;
import com.trackasia.navigation.core.routeprogress.RouteProgress;

class FpsDelegateProgressChangeListener implements ProgressChangeListener {

  private final MapFpsDelegate fpsDelegate;

  FpsDelegateProgressChangeListener(MapFpsDelegate fpsDelegate) {
    this.fpsDelegate = fpsDelegate;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    fpsDelegate.adjustFpsFor(routeProgress);
  }
}
