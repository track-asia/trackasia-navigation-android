package com.trackasia.navigation.android.navigation.ui.v5.instruction;

import com.trackasia.navigation.android.navigation.v5.models.BannerComponents;

class TextVerifier implements NodeVerifier {
  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return bannerComponents.getText() != null && !bannerComponents.getText().isEmpty();
  }
}
