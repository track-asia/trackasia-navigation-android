package com.trackasia.navigation.android.navigation.ui.v5;

import androidx.annotation.NonNull;

import com.trackasia.navigation.core.location.Location;
import com.trackasia.navigation.core.models.DirectionsRoute;
import com.trackasia.geojson.Point;

public interface NavigationContract {

  interface View {

    void setSummaryBehaviorState(int state);

    void setSummaryBehaviorHideable(boolean isHideable);

    boolean isSummaryBottomSheetHidden();

    void updateWayNameVisibility(boolean isVisible);

    void updateWayNameView(@NonNull String wayName);

    void resetCameraPosition();

    void showRecenterBtn();

    void hideRecenterBtn();

    void drawRoute(DirectionsRoute directionsRoute);

    void addMarker(Point point);

    void startCamera(DirectionsRoute directionsRoute);

    void resumeCamera(Location location);

    void updateNavigationMap(Location location);

    boolean isRecenterButtonVisible();

    void updateCameraRouteOverview();
  }
}
