package com.trackasia.navigation.android.navigation.ui.v5.map;

import android.graphics.PointF;

import com.trackasia.geojson.Feature;
import com.trackasia.android.maps.TrackAsiaMap;

import java.util.List;

class WaynameFeatureFinder {

  private TrackAsiaMap trackAsiaMap;

  WaynameFeatureFinder(TrackAsiaMap trackAsiaMap) {
    this.trackAsiaMap = trackAsiaMap;
  }

  List<Feature> queryRenderedFeatures(PointF point, String[] layerIds) {
    return trackAsiaMap.queryRenderedFeatures(point, layerIds);
  }
}
