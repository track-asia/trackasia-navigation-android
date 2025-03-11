package com.trackasia.navigation.android.navigation.ui.v5.route;

import com.trackasia.geojson.FeatureCollection;

import java.util.List;

interface OnPrimaryRouteUpdatedCallback {
  void onPrimaryRouteUpdated(List<FeatureCollection> updatedRouteCollections);
}
