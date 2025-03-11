package com.trackasia.navigation.android.navigation.ui.v5.route;

import com.trackasia.navigation.android.navigation.v5.models.DirectionsRoute;
import com.trackasia.geojson.FeatureCollection;
import com.trackasia.geojson.LineString;

import java.util.HashMap;
import java.util.List;

interface OnRouteFeaturesProcessedCallback {
  void onRouteFeaturesProcessed(List<FeatureCollection> routeFeatureCollections,
                                HashMap<LineString, DirectionsRoute> routeLineStrings);
}
