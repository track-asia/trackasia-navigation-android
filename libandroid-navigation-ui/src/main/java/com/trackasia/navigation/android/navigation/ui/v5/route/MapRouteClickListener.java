package com.trackasia.navigation.android.navigation.ui.v5.route;

import androidx.annotation.NonNull;

import com.trackasia.navigation.android.navigation.v5.models.DirectionsRoute;
import com.trackasia.geojson.Feature;
import com.trackasia.geojson.LineString;
import com.trackasia.geojson.Point;
import com.trackasia.android.geometry.LatLng;
import com.trackasia.android.maps.TrackAsiaMap;
import com.trackasia.turf.TurfConstants;
import com.trackasia.turf.TurfMeasurement;
import com.trackasia.turf.TurfMisc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class MapRouteClickListener implements TrackAsiaMap.OnMapClickListener {

  private final MapRouteLine routeLine;

  private OnRouteSelectionChangeListener onRouteSelectionChangeListener;
  private boolean alternativesVisible = true;

  MapRouteClickListener(MapRouteLine routeLine) {
    this.routeLine = routeLine;
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    if (!isRouteVisible()) {
      return false;
    }
    HashMap<LineString, DirectionsRoute> routeLineStrings = routeLine.retrieveRouteLineStrings();
    if (invalidMapClick(routeLineStrings)) {
      return false;
    }
    List<DirectionsRoute> directionsRoutes = routeLine.retrieveDirectionsRoutes();
    findClickedRoute(point, routeLineStrings, directionsRoutes);
    return false;
  }

  void setOnRouteSelectionChangeListener(OnRouteSelectionChangeListener listener) {
    onRouteSelectionChangeListener = listener;
  }

  void updateAlternativesVisible(boolean alternativesVisible) {
    this.alternativesVisible = alternativesVisible;
  }

  private boolean invalidMapClick(HashMap<LineString, DirectionsRoute> routeLineStrings) {
    return routeLineStrings == null || routeLineStrings.isEmpty() || !alternativesVisible;
  }

  private boolean isRouteVisible() {
    return routeLine.retrieveVisibility();
  }

  private void findClickedRoute(@NonNull LatLng point, HashMap<LineString, DirectionsRoute> routeLineStrings,
                                List<DirectionsRoute> directionsRoutes) {

    HashMap<Double, DirectionsRoute> routeDistancesAwayFromClick = new HashMap<>();
    Point clickPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

    calculateClickDistances(routeDistancesAwayFromClick, clickPoint, routeLineStrings);
    List<Double> distancesAwayFromClick = new ArrayList<>(routeDistancesAwayFromClick.keySet());
    Collections.sort(distancesAwayFromClick);

    DirectionsRoute clickedRoute = routeDistancesAwayFromClick.get(distancesAwayFromClick.get(0));
    int newPrimaryRouteIndex = directionsRoutes.indexOf(clickedRoute);
    if (routeLine.updatePrimaryRouteIndex(newPrimaryRouteIndex) && onRouteSelectionChangeListener != null) {
      DirectionsRoute selectedRoute = directionsRoutes.get(newPrimaryRouteIndex);
      onRouteSelectionChangeListener.onNewPrimaryRouteSelected(selectedRoute);
    }
  }

  private void calculateClickDistances(HashMap<Double, DirectionsRoute> routeDistancesAwayFromClick,
                                       Point clickPoint, HashMap<LineString, DirectionsRoute> routeLineStrings) {
    for (LineString lineString : routeLineStrings.keySet()) {
      Point pointOnLine = findPointOnLine(clickPoint, lineString);
      if (pointOnLine == null) {
        return;
      }
      double distance = TurfMeasurement.distance(clickPoint, pointOnLine, TurfConstants.UNIT_METERS);
      routeDistancesAwayFromClick.put(distance, routeLineStrings.get(lineString));
    }
  }

  private Point findPointOnLine(Point clickPoint, LineString lineString) {
    List<Point> linePoints = lineString.coordinates();
    Feature feature = TurfMisc.nearestPointOnLine(clickPoint, linePoints);
    return (Point) feature.geometry();
  }
}
