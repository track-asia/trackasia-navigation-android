package com.trackasia.navigation.android.navigation.ui.v5.camera;

import com.trackasia.android.camera.CameraUpdate;
import com.trackasia.android.location.modes.CameraMode;
import com.trackasia.android.maps.TrackAsiaMap;

class CameraAnimationDelegate {

  private final TrackAsiaMap trackAsiaMap;

  CameraAnimationDelegate(TrackAsiaMap trackAsiaMap) {
    this.trackAsiaMap = trackAsiaMap;
  }

  void render(NavigationCameraUpdate update, int durationMs, TrackAsiaMap.CancelableCallback callback) {
    CameraUpdateMode mode = update.getMode();
    CameraUpdate cameraUpdate = update.getCameraUpdate();
    if (mode == CameraUpdateMode.OVERRIDE) {
      trackAsiaMap.getLocationComponent().setCameraMode(CameraMode.NONE);
      trackAsiaMap.animateCamera(cameraUpdate, durationMs, callback);
    } else if (!isTracking()) {
      trackAsiaMap.animateCamera(cameraUpdate, durationMs, callback);
    }
  }

  private boolean isTracking() {
    int cameraMode = trackAsiaMap.getLocationComponent().getCameraMode();
    return cameraMode != CameraMode.NONE;
  }
}