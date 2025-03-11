package com.trackasia.navigation.android.navigation.ui.v5.map;

import static com.trackasia.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.TRACKASIA_NAVIGATION_MARKER_NAME;

import android.graphics.Bitmap;

import com.trackasia.android.maps.MapView;
import com.trackasia.android.maps.TrackAsiaMap;

class SymbolOnStyleLoadedListener implements MapView.OnDidFinishLoadingStyleListener {

  private final TrackAsiaMap trackAsiaMap;
  private final Bitmap markerBitmap;

  SymbolOnStyleLoadedListener(TrackAsiaMap trackAsiaMap, Bitmap markerBitmap) {
    this.trackAsiaMap = trackAsiaMap;
    this.markerBitmap = markerBitmap;
  }

  @Override
  public void onDidFinishLoadingStyle() {
    trackAsiaMap.getStyle().addImage(TRACKASIA_NAVIGATION_MARKER_NAME, markerBitmap);
  }
}
