package com.trackasia.navigation.android.navigation.ui.v5.camera;

import com.trackasia.android.camera.CameraUpdate;
import com.trackasia.android.maps.TrackAsiaMap;

class CameraOverviewCancelableCallback implements TrackAsiaMap.CancelableCallback {

  private static final int OVERVIEW_UPDATE_DURATION_IN_MILLIS = 750;

  private CameraUpdate overviewUpdate;
  private TrackAsiaMap trackAsiaMap;

  CameraOverviewCancelableCallback(CameraUpdate overviewUpdate, TrackAsiaMap trackAsiaMap) {
    this.overviewUpdate = overviewUpdate;
    this.trackAsiaMap = trackAsiaMap;
  }

  @Override
  public void onCancel() {
    // No-op
  }

  @Override
  public void onFinish() {
    trackAsiaMap.animateCamera(overviewUpdate, OVERVIEW_UPDATE_DURATION_IN_MILLIS);
  }
}
