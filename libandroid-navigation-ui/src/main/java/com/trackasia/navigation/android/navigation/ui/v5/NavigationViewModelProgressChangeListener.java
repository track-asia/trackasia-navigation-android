package com.trackasia.navigation.android.navigation.ui.v5;

import com.trackasia.navigation.core.location.Location;
import com.trackasia.navigation.core.routeprogress.ProgressChangeListener;
import com.trackasia.navigation.core.routeprogress.RouteProgress;

class NavigationViewModelProgressChangeListener implements ProgressChangeListener {

  private final NavigationViewModel viewModel;

  NavigationViewModelProgressChangeListener(NavigationViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    viewModel.updateRouteProgress(routeProgress);
    viewModel.updateLocation(location);
  }
}