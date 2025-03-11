package com.trackasia.navigation.android.navigation.ui.v5.map;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.trackasia.geojson.Point;
import com.trackasia.android.geometry.LatLng;
import com.trackasia.android.plugins.annotation.Symbol;
import com.trackasia.android.plugins.annotation.SymbolManager;
import com.trackasia.android.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

class NavigationSymbolManager {

  static final String TRACKASIA_NAVIGATION_MARKER_NAME = "trackasia-navigation-marker";
  private final List<Symbol> mapMarkersSymbols = new ArrayList<>();
  private final SymbolManager symbolManager;

  NavigationSymbolManager(SymbolManager symbolManager) {
    this.symbolManager = symbolManager;
    symbolManager.setIconAllowOverlap(true);
    symbolManager.setIconIgnorePlacement(true);
  }

  void addDestinationMarkerFor(Point position) {
    SymbolOptions options = createSymbolOptionsFor(position);
    createSymbolFrom(options);
  }

  void addCustomSymbolFor(SymbolOptions options) {
    createSymbolFrom(options);
  }

  void removeAllMarkerSymbols() {
    for (Symbol markerSymbol : mapMarkersSymbols) {
      symbolManager.delete(markerSymbol);
    }
    mapMarkersSymbols.clear();
  }

  @NonNull
  private SymbolOptions createSymbolOptionsFor(Point position) {
    LatLng markerPosition = new LatLng(position.latitude(),
      position.longitude());
    return new SymbolOptions()
      .withLatLng(markerPosition)
      .withIconImage(TRACKASIA_NAVIGATION_MARKER_NAME);
  }

  private void createSymbolFrom(SymbolOptions options) {
    Symbol symbol = symbolManager.create(options);
    mapMarkersSymbols.add(symbol);
  }

  @UiThread
  public void onDestroy() {
    symbolManager.onDestroy();
  }
}
