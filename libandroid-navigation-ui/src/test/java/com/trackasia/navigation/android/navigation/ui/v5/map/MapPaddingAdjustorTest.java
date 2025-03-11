package com.trackasia.navigation.android.navigation.ui.v5.map;

import com.trackasia.android.maps.TrackAsiaMap;

import org.junit.Test;
import com.trackasia.navigation.android.navigation.ui.v5.map.MapPaddingAdjustor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MapPaddingAdjustorTest {

  @Test
  public void adjustLocationIconWith_customPaddingIsSet() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    verify(trackAsiaMap).setPadding(0, 0, 0, 0);
  }

  @Test
  public void isUsingDefault_falseAfterCustomPaddingIsSet() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    assertFalse(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void isUsingDefault_trueWithoutCustomPadding() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};

    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);

    assertTrue(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void updatePaddingWithZero_updatesMapToZeroPadding() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);

    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    verify(trackAsiaMap).setPadding(0, 0, 0, 0);
  }

  @Test
  public void updatePaddingWithZero_retainsCustomPadding() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 350, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);
    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    paddingAdjustor.resetPadding();

    verify(trackAsiaMap, times(2)).setPadding(0, 350, 0, 0);
  }

  @Test
  public void updatePaddingWithDefault_defaultIsRestoredAfterCustom() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);

    paddingAdjustor.updatePaddingWithDefault();

    verify(trackAsiaMap).setPadding(0, 250, 0, 0);
  }

  @Test
  public void retrieveCurrentPadding_returnsCurrentMapPadding() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(trackAsiaMap, defaultPadding);

    paddingAdjustor.retrieveCurrentPadding();

    verify(trackAsiaMap).getPadding();
  }
}