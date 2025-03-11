package com.trackasia.navigation.android.navigation.ui.v5.map;

import android.graphics.PointF;

import com.trackasia.android.maps.TrackAsiaMap;

import org.junit.Test;
import com.trackasia.navigation.android.navigation.ui.v5.map.WaynameFeatureFinder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WaynameFeatureFinderTest {

  @Test
  public void queryRenderedFeatures_trackAsiaMapIsCalled() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    WaynameFeatureFinder featureFinder = new WaynameFeatureFinder(trackAsiaMap);
    PointF point = mock(PointF.class);
    String[] layerIds = {"id", "id"};

    featureFinder.queryRenderedFeatures(point, layerIds);

    verify(trackAsiaMap).queryRenderedFeatures(point, layerIds);
  }
}