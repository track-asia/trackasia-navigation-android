package com.trackasia.navigation.android.navigation.ui.v5.route;

import com.trackasia.geojson.FeatureCollection;
import com.trackasia.android.style.sources.GeoJsonOptions;
import com.trackasia.android.style.sources.GeoJsonSource;

class MapRouteSourceProvider {

  GeoJsonSource build(String id, FeatureCollection featureCollection, GeoJsonOptions options) {
    return new GeoJsonSource(id, featureCollection, options);
  }
}
