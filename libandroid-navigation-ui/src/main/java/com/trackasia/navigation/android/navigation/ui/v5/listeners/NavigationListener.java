package com.trackasia.navigation.android.navigation.ui.v5.listeners;

import com.trackasia.navigation.android.navigation.ui.v5.NavigationView;
import com.trackasia.navigation.android.navigation.ui.v5.NavigationViewOptions;
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation;

/**
 * A listener that can be implemented and
 * added to {@link NavigationViewOptions} to
 * hook into navigation related events occurring in {@link NavigationView}.
 */
public interface NavigationListener {

  /**
   * Will be triggered when the user clicks
   * on the cancel "X" icon while navigating.
   *
   * @since 0.8.0
   */
  void onCancelNavigation();

  /**
   * Will be triggered when {@link TrackAsiaNavigation}
   * has finished and the service is completely shut down.
   *
   * @since 0.8.0
   */
  void onNavigationFinished();

  /**
   * Will be triggered when {@link TrackAsiaNavigation}
   * has been initialized and the user is navigating the given route.
   *
   * @since 0.8.0
   */
  void onNavigationRunning();
}
