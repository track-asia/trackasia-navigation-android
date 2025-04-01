package com.trackasia.navigation.android.navigation.ui.v5.map;

import static com.trackasia.navigation.android.navigation.ui.v5.GeoJsonExtKt.toJvmPoints;

import com.trackasia.navigation.core.location.Location;

import com.trackasia.navigation.core.routeprogress.ProgressChangeListener;
import com.trackasia.navigation.core.routeprogress.RouteProgress;

class MapWaynameProgressChangeListener implements ProgressChangeListener {

  private final MapWayName mapWayName;

  MapWaynameProgressChangeListener(MapWayName mapWayName) {
    this.mapWayName = mapWayName;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    mapWayName.updateProgress(location, toJvmPoints(routeProgress.getCurrentStepPoints()));
  }
}
