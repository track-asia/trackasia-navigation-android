package com.trackasia.navigation.android.navigation.ui.v5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.trackasia.navigation.android.navigation.ui.v5.listeners.NavigationListener;
import com.trackasia.navigation.core.models.DirectionsRoute;
import com.trackasia.android.camera.CameraPosition;
import com.trackasia.navigation.core.navigation.TrackAsiaNavigationOptions;
import com.trackasia.navigation.core.navigation.NavigationConstants;

/**
 * Serves as a launching point for the custom drop-in UI, {@link NavigationView}.
 * <p>
 * Demonstrates the proper setup and usage of the view, including all lifecycle methods.
 */
public class TrackAsiaNavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback, NavigationListener {

  private NavigationView navigationView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_navigation);
    navigationView = findViewById(R.id.navigationView);
    navigationView.onCreate(savedInstanceState);
    initialize();
  }

  @Override
  public void onStart() {
    super.onStart();
    navigationView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    navigationView.onResume();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    navigationView.onLowMemory();
  }

  @Override
  public void onBackPressed() {
    // If the navigation view didn't need to do anything, call super
    if (!navigationView.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    navigationView.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    navigationView.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  public void onPause() {
    super.onPause();
    navigationView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    navigationView.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    navigationView.onDestroy();
  }

  @Override
  public void onNavigationReady(boolean isRunning) {
    try {
      NavigationViewOptions.Builder options = NavigationViewOptions.builder();
      options.navigationListener(this);
      
      DirectionsRoute route = extractRoute(options);
      if (route == null) {
        android.util.Log.e("TrackAsiaNavigationActivity", "Failed to extract route, cannot start navigation");
        finish();
        return;
      }
      
      extractConfiguration(options);
      options.navigationOptions(new TrackAsiaNavigationOptions());
      navigationView.startNavigation(options.build());
    } catch (Exception e) {
      android.util.Log.e("TrackAsiaNavigationActivity", "Error starting navigation: " + e.getMessage());
      finish();
    }
  }

  @Override
  public void onCancelNavigation() {
    finishNavigation();
  }

  @Override
  public void onNavigationFinished() {
    finishNavigation();
  }

  @Override
  public void onNavigationRunning() {
    // Intentionally empty
  }

  private void initialize() {
    Parcelable position = getIntent().getParcelableExtra(NavigationConstants.NAVIGATION_VIEW_INITIAL_MAP_POSITION);
    if (position != null) {
      navigationView.initialize(this, (CameraPosition) position);
    } else {
      navigationView.initialize(this);
    }
  }

  private DirectionsRoute extractRoute(NavigationViewOptions.Builder options) {
    DirectionsRoute route = NavigationLauncher.extractRoute(this);
    if (route != null) {
      options.directionsRoute(route);
    }
    return route;
  }

  private void extractConfiguration(NavigationViewOptions.Builder options) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    options.shouldSimulateRoute(preferences.getBoolean(NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE, false));
  }

  private void finishNavigation() {
    NavigationLauncher.cleanUpPreferences(this);
    finish();
  }
}
