package com.trackasia.navigation.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import com.trackasia.android.maps.TrackAsiaMap;
import com.trackasia.android.maps.Style;

import org.junit.Test;
import com.trackasia.navigation.android.navigation.ui.v5.map.SymbolOnStyleLoadedListener;

import static com.trackasia.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.TRACKASIA_NAVIGATION_MARKER_NAME;
import static com.trackasia.navigation.android.navigation.ui.v5.map.NavigationSymbolManager.TRACKASIA_NAVIGATION_MARKER_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SymbolOnStyleLoadedListenerTest {

  @Test
  public void onDidFinishLoadingStyle_markerIsAdded() {
    TrackAsiaMap trackAsiaMap = mock(TrackAsiaMap.class);
    Style style = mock(Style.class);
    when(trackAsiaMap.getStyle()).thenReturn(style);
    Bitmap markerBitmap = mock(Bitmap.class);
    SymbolOnStyleLoadedListener listener = new SymbolOnStyleLoadedListener(trackAsiaMap, markerBitmap);

    listener.onDidFinishLoadingStyle();

    verify(style).addImage(eq(TRACKASIA_NAVIGATION_MARKER_NAME), eq(markerBitmap));
  }
}