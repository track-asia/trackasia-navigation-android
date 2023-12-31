package com.mapbox.services.android.navigation.ui.v5.camera;

import com.trackasia.android.maps.TrackasiaMap;

class ResetCancelableCallback implements TrackasiaMap.CancelableCallback {

  private final NavigationCamera camera;

  ResetCancelableCallback(NavigationCamera camera) {
    this.camera = camera;
  }

  @Override
  public void onCancel() {
    camera.updateIsResetting(false);
  }

  @Override
  public void onFinish() {
    camera.updateIsResetting(false);
  }
}