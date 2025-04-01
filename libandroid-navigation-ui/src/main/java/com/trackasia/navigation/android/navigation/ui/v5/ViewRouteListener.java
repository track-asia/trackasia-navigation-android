package com.trackasia.navigation.android.navigation.ui.v5;

import com.trackasia.navigation.core.models.DirectionsRoute;
import com.trackasia.geojson.Point;

public interface ViewRouteListener {

  void onRouteUpdate(DirectionsRoute directionsRoute);

  void onRouteRequestError(String errorMessage);

  void onDestinationSet(Point destination);
}
