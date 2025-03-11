package com.trackasia.navigation.android.navigation.ui.v5.map;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationTrackAsiaMapInstanceState implements Parcelable {

  private final NavigationMapSettings settings;

  NavigationTrackAsiaMapInstanceState(NavigationMapSettings settings) {
    this.settings = settings;
  }

  NavigationMapSettings retrieveSettings() {
    return settings;
  }

  private NavigationTrackAsiaMapInstanceState(Parcel in) {
    settings = in.readParcelable(NavigationMapSettings.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(settings, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<NavigationTrackAsiaMapInstanceState> CREATOR =
    new Creator<NavigationTrackAsiaMapInstanceState>() {
      @Override
      public NavigationTrackAsiaMapInstanceState createFromParcel(Parcel in) {
        return new NavigationTrackAsiaMapInstanceState(in);
      }

      @Override
      public NavigationTrackAsiaMapInstanceState[] newArray(int size) {
        return new NavigationTrackAsiaMapInstanceState[size];
      }
    };
}
