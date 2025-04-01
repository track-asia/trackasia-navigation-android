package com.trackasia.navigation.android.navigation.ui.v5.instruction;

import android.text.TextUtils;

import com.trackasia.navigation.core.models.BannerComponents;

class AbbreviationVerifier implements NodeVerifier {
  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return hasAbbreviation(bannerComponents);
  }

  private boolean hasAbbreviation(BannerComponents components) {
    return !TextUtils.isEmpty(components.getAbbreviation());
  }
}
