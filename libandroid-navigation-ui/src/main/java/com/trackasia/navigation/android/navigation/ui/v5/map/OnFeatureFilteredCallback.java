package com.trackasia.navigation.android.navigation.ui.v5.map;

import androidx.annotation.NonNull;

import com.trackasia.geojson.Feature;

interface OnFeatureFilteredCallback {
  void onFeatureFiltered(@NonNull Feature feature);
}
