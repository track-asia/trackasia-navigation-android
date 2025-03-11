package com.trackasia.navigation.android.navigation.ui.v5.camera;

import com.trackasia.android.camera.CameraPosition;
import com.trackasia.android.camera.CameraUpdate;
import com.trackasia.android.location.LocationComponent;
import com.trackasia.android.location.OnLocationCameraTransitionListener;
import com.trackasia.android.location.modes.CameraMode;
import com.trackasia.android.maps.TrackAsiaMap;
import com.trackasia.navigation.android.navigation.ui.v5.BaseTest;

import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigation;
import com.trackasia.navigation.android.navigation.v5.navigation.camera.RouteInformation;
import com.trackasia.navigation.android.navigation.v5.routeprogress.ProgressChangeListener;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NavigationCameraTest extends BaseTest {

  @Test
  public void sanity() {
    NavigationCamera camera = buildCamera();

    assertNotNull(camera);
  }

  @Test
  public void setTrackingEnabled_trackingIsEnabled() {
    LocationComponent locationComponent = mock(LocationComponent.class);
    NavigationCamera camera = buildCamera(locationComponent);

    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));
    verify(locationComponent, times(0)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    verify(locationComponent, times(2)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));

    assertTrue(camera.isTrackingEnabled());
  }

  @Test
  public void setTrackingDisabled_trackingIsDisabled() {
    LocationComponent locationComponent = mock(LocationComponent.class);
    NavigationCamera camera = buildCamera(locationComponent);

    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));
    verify(locationComponent, times(0)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    verify(locationComponent, times(2)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    assertFalse(camera.isTrackingEnabled());
  }

  @Test
  public void onResetCamera_trackingIsResumed() {
    NavigationCamera camera = buildCamera();

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    camera.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);

    assertTrue(camera.isTrackingEnabled());
  }

  @Test
  public void onResetCamera_dynamicCameraIsReset() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    when(trackAsiaMap.getCameraPosition()).thenReturn(mock(CameraPosition.class));
    TrackAsiaNavigation navigation = mock(TrackAsiaNavigation.class);
    DynamicCamera dynamicCamera = mock(DynamicCamera.class);
    when(navigation.getCameraEngine()).thenReturn(dynamicCamera);
    RouteInformation currentRouteInformation = mock(RouteInformation.class);
    NavigationCamera camera = buildCamera(trackAsiaMap, navigation, currentRouteInformation);

    camera.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);

    verify(dynamicCamera).forceResetZoomLevel();
  }

  @Test
  public void onStartWithNullRoute_progressListenerIsAdded() {
    TrackAsiaNavigation navigation = mock(TrackAsiaNavigation.class);
    ProgressChangeListener listener = mock(ProgressChangeListener.class);
    NavigationCamera camera = buildCamera(navigation, listener);

    camera.start(null);

    verify(navigation, times(1)).addProgressChangeListener(listener);
  }

  @Test
  public void onResumeWithNullLocation_progressListenerIsAdded() {
    TrackAsiaNavigation navigation = mock(TrackAsiaNavigation.class);
    ProgressChangeListener listener = mock(ProgressChangeListener.class);
    NavigationCamera camera = buildCamera(navigation, listener);

    camera.resume(null);

    verify(navigation, times(1)).addProgressChangeListener(listener);
  }

  @Test
  public void update_defaultIsIgnoredWhileTracking() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(trackAsiaMap.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    TrackAsiaMap.CancelableCallback callback = mock(TrackAsiaMap.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    NavigationCamera camera = buildCamera(trackAsiaMap);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(trackAsiaMap, times(0)).animateCamera(cameraUpdate);
  }

  @Test
  public void update_defaultIsAcceptedWithNoTracking() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.NONE);
    when(trackAsiaMap.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    TrackAsiaMap.CancelableCallback callback = mock(TrackAsiaMap.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    NavigationCamera camera = buildCamera(trackAsiaMap);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(trackAsiaMap).animateCamera(eq(cameraUpdate), eq(300), eq(callback));
  }

  @Test
  public void update_overrideIsAcceptedWhileTracking() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(trackAsiaMap.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    TrackAsiaMap.CancelableCallback callback = mock(TrackAsiaMap.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
    NavigationCamera camera = buildCamera(trackAsiaMap);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(trackAsiaMap).animateCamera(eq(cameraUpdate), eq(300), eq(callback));
  }

  @Test
  public void update_overrideSetsLocationComponentCameraModeNone() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(trackAsiaMap.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    TrackAsiaMap.CancelableCallback callback = mock(TrackAsiaMap.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
    NavigationCamera camera = buildCamera(trackAsiaMap);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(locationComponent).setCameraMode(eq(CameraMode.NONE));
  }

  private NavigationCamera buildCamera() {
    return new NavigationCamera(mock(TrackAsiaMap.class), mock(TrackAsiaNavigation.class), mock(LocationComponent.class));
  }

  private NavigationCamera buildCamera(TrackAsiaMap trackAsiaMap) {
    return new NavigationCamera(trackAsiaMap, mock(TrackAsiaNavigation.class), mock(LocationComponent.class));
  }

  private NavigationCamera buildCamera(LocationComponent locationComponent) {
    return new NavigationCamera(mock(TrackAsiaMap.class), mock(TrackAsiaNavigation.class), locationComponent);
  }

  private NavigationCamera buildCamera(TrackAsiaNavigation navigation, ProgressChangeListener listener) {
    return new NavigationCamera(mock(TrackAsiaMap.class), navigation, listener,
      mock(LocationComponent.class), mock(RouteInformation.class));
  }

  private NavigationCamera buildCamera(TrackAsiaMap trackAsiaMap, TrackAsiaNavigation navigation,
                                       RouteInformation routeInformation) {
    return new NavigationCamera(trackAsiaMap, navigation, mock(ProgressChangeListener.class),
      mock(LocationComponent.class), routeInformation);
  }
}
