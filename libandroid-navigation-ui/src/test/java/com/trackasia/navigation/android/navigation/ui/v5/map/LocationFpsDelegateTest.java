package com.trackasia.navigation.android.navigation.ui.v5.map;

import com.trackasia.android.camera.CameraPosition;
import com.trackasia.android.location.LocationComponent;
import com.trackasia.android.maps.TrackAsiaMap;

import org.junit.Test;
import com.trackasia.navigation.android.navigation.ui.v5.map.LocationFpsDelegate;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationFpsDelegateTest {

  @Test
  public void onCameraIdle_newFpsIsSetZoom16() {
    double zoom = 16d;
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    when(trackAsiaMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(25));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom14() {
    double zoom = 14d;
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    when(trackAsiaMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(15));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom10() {
    double zoom = 10d;
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    when(trackAsiaMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(10));
  }

  @Test
  public void onCameraIdle_newFpsIsSet5() {
    double zoom = 5d;
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    when(trackAsiaMap.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(5));
  }

  @Test
  public void onStart_idleListenerAdded() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onStart();

    verify(trackAsiaMap, times(2)).addOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void onStop_idleListenerRemoved() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.onStop();

    verify(trackAsiaMap).removeOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void updateEnabled_falseResetsToMax() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    verify(locationComponent).setMaxAnimationFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void isEnabled_returnsFalseWhenSet() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(trackAsiaMap, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    assertFalse(locationFpsDelegate.isEnabled());
  }

  private CameraPosition buildCameraPosition(double zoom) {
    return new CameraPosition.Builder().zoom(zoom).build();
  }
}