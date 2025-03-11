package com.trackasia.navigation.android.navigation.ui.v5.route;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trackasia.navigation.android.navigation.v5.models.DirectionsRoute;
import com.trackasia.android.maps.MapView;
import com.trackasia.android.maps.TrackAsiaMap;
import com.trackasia.navigation.android.navigation.v5.navigation.TrackAsiaNavigation;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class NavigationMapRouteTest {

  @Test
  public void checksMapClickListenerIsAddedAtConstructionTime() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);

    new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap, mockedStyleRes, "",
      mockedMapClickListener, mockedDidFinishLoadingStyleListener, mockedProgressChangeListener);

    verify(mockedTrackAsiaMap, times(1)).addOnMapClickListener(eq(mockedMapClickListener));
  }

  @Test
  public void checksDidFinishLoadingStyleListenerIsAddedAtConstructionTime() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);

    new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap, mockedStyleRes, "",
      mockedMapClickListener, mockedDidFinishLoadingStyleListener, mockedProgressChangeListener);

    verify(mockedMapView, times(1))
      .addOnDidFinishLoadingStyleListener(eq(mockedDidFinishLoadingStyleListener));
  }

  @Test
  public void checksMapRouteProgressChangeListenerIsAddedAtConstructionTime() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);

    new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap, mockedStyleRes, "",
      mockedMapClickListener, mockedDidFinishLoadingStyleListener, mockedProgressChangeListener);

    verify(mockedNavigation, times(1))
      .addProgressChangeListener(eq(mockedProgressChangeListener));
  }

  @Test
  public void checksMapClickListenerIsNotAddedIfIsMapClickListenerAdded() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView,
      mockedTrackAsiaMap, mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener);

    theNavigationMapRoute.onStart();

    verify(mockedTrackAsiaMap, times(1)).addOnMapClickListener(eq(mockedMapClickListener));
  }

  @Test
  public void checksDidFinishLoadingStyleListenerIsNotAddedIfIsDidFinishLoadingStyleListenerAdded() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView,
      mockedTrackAsiaMap, mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener);

    theNavigationMapRoute.onStart();

    verify(mockedMapView, times(1))
      .addOnDidFinishLoadingStyleListener(eq(mockedDidFinishLoadingStyleListener));
  }

  @Test
  public void checksMapClickListenerIsRemovedInOnStop() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView,
      mockedTrackAsiaMap, mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener);

    theNavigationMapRoute.onStop();

    verify(mockedTrackAsiaMap, times(1)).removeOnMapClickListener(eq(mockedMapClickListener));
  }

  @Test
  public void checksDidFinishLoadingStyleListenerIsRemovedInOnStop() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView,
      mockedTrackAsiaMap, mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener);

    theNavigationMapRoute.onStop();

    verify(mockedMapView, times(1))
      .removeOnDidFinishLoadingStyleListener(eq(mockedDidFinishLoadingStyleListener));
  }

  @Test
  public void checksMapRouteProgressChangeListenerIsRemovedInOnStop() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView,
      mockedTrackAsiaMap, mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener);

    theNavigationMapRoute.onStop();

    verify(mockedNavigation, times(1))
      .removeProgressChangeListener(eq(mockedProgressChangeListener));
  }

  @Test
  public void addProgressChangeListener_mapRouteProgressChangeListenerIsAdded() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);

    theNavigationMapRoute.addProgressChangeListener(mockedNavigation);

    verify(mockedNavigation, times(1))
      .addProgressChangeListener(eq(mockedProgressChangeListener));
  }

  @Test
  public void removeProgressChangeListener_mapRouteProgressChangeListenerIsRemoved() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);

    theNavigationMapRoute.removeProgressChangeListener(mockedNavigation);

    verify(mockedNavigation, times(1))
      .removeProgressChangeListener(eq(mockedProgressChangeListener));
  }

  @Test
  public void addRoutes_mapRouteProgressChangeListenerIsAdded() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    List<DirectionsRoute> routes = Collections.emptyList();
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);

    theNavigationMapRoute.addRoutes(routes);

    verify(mockedMapRouteLine).draw(eq(routes));
  }

  @Test
  public void updateRouteVisibilityTo_routeLineVisibilityIsUpdated() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);
    boolean isVisible = false;

    theNavigationMapRoute.updateRouteVisibilityTo(isVisible);

    verify(mockedMapRouteLine).updateVisibilityTo(isVisible);
  }

  @Test
  public void removeRoute_routeLineVisibilityIsUpdated() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);

    theNavigationMapRoute.removeRoute();

    verify(mockedMapRouteLine).updateVisibilityTo(false);
  }

  @Test
  public void removeRoute_routeArrowVisibilityIsUpdated() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);

    theNavigationMapRoute.removeRoute();

    verify(mockedMapRouteArrow).updateVisibilityTo(false);
  }

  @Test
  public void updateRouteVisibilityTo_progressChangeVisibilityIsUpdated() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);
    boolean isVisible = false;

    theNavigationMapRoute.updateRouteVisibilityTo(isVisible);

    verify(mockedProgressChangeListener).updateVisibility(isVisible);
  }

  @Test
  public void updateRouteArrowVisibilityTo_routeArrowReceivesNewVisibility() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);
    boolean isVisible = false;

    theNavigationMapRoute.updateRouteArrowVisibilityTo(isVisible);

    verify(mockedMapRouteArrow).updateVisibilityTo(isVisible);
  }

  @Test
  public void showAlternativeRoutes_mapRouteProgressChangeListenerIsAdded() {
    TrackAsiaNavigation mockedNavigation = mock(TrackAsiaNavigation.class);
    MapView mockedMapView = mock(MapView.class);
    TrackAsiaMap mockedTrackAsiaMap = mock(TrackAsiaMap.class);
    int mockedStyleRes = 0;
    MapRouteClickListener mockedMapClickListener = mock(MapRouteClickListener.class);
    MapView.OnDidFinishLoadingStyleListener mockedDidFinishLoadingStyleListener =
      mock(MapView.OnDidFinishLoadingStyleListener.class);
    MapRouteProgressChangeListener mockedProgressChangeListener = mock(MapRouteProgressChangeListener.class);
    MapRouteLine mockedMapRouteLine = mock(MapRouteLine.class);
    MapRouteArrow mockedMapRouteArrow = mock(MapRouteArrow.class);
    NavigationMapRoute theNavigationMapRoute = new NavigationMapRoute(mockedNavigation, mockedMapView, mockedTrackAsiaMap,
      mockedStyleRes, "", mockedMapClickListener, mockedDidFinishLoadingStyleListener,
      mockedProgressChangeListener, mockedMapRouteLine, mockedMapRouteArrow);
    boolean isVisible = false;

    theNavigationMapRoute.showAlternativeRoutes(isVisible);

    verify(mockedMapRouteLine).toggleAlternativeVisibilityWith(isVisible);
  }
}
