<p align="center">
  <a href="https://jitpack.io/#trackasia/trackasia-navigation-android">
    <img src="https://jitpack.io/v/track-asia/trackasia-navigation-android.svg"
         alt="Jitpack">
  </a>
</p>

# Maplibre Navigation SDK for Android

The Maplibre Navigation SDK for Android is built on a fork of the [Mapbox Navigation SDK v0.19](https://github.com/flitsmeister/flitsmeister-navigation-android/tree/v0.19.0) which is built on top of the [Mapbox Directions API](https://www.mapbox.com/directions) and contains the logic needed to get timed navigation instructions.

With this SDK you can implement turn-by-turn navigation in your own Android app while hosting your Map tiles and Directions API.


<div align="center">
  <img src="https://github.com/track-asia/trackasia-navigation-android/blob/main/.github/preview.png" height="350px" alt="TrackAsia Navigation Android">
</div>

## License

- 100% Open Source
- [MIT License](LICENSE)
- No Telemetry


## Why have we forked

1. Mapbox decided to put a closed-source component to their navigation SDK and introduced a non-open-source license. Maplibre wants an open-source solution.
2. Mapbox decided to put telemetry in their SDK. We couldn't turn this off without adjusting the source.
3. We want to use the SDK without paying Mapbox for each MAU and without Mapbox API keys.

All issues are covered with this SDK. 

## What have we changed

- We completely removed the UI part from the SDK so it will only contain the logic for navigation and not the visuals.
- We upgraded the [Mapbox Maps SDK for Android](https://github.com/mapbox/mapbox-gl-native/tree/master/platform/android) to [TrackAsia Native for Android](https://github.com/track-asia/trackasia-gl-native/tree/master/platform/android) version 9.4.0.
- We upgraded the [NavigationRoute](https://github.com/flitsmeister/flitsmeister-navigation-android/blob/master/libandroid-navigation/src/main/java/com/mapbox/services/android/navigation/v5/navigation/NavigationRoute.java#L425) 
 with the possibility to add an interceptor to the request.
- We changed the [locationLayerPlugin](https://github.com/mapbox/mapbox-plugins-android) to the [location component](https://docs.mapbox.com/android/api/map-sdk/8.5.0/com/mapbox/mapboxsdk/location/LocationComponent.html)
- We updated the logic around the implementation of the locationEngine so it can be used with the new locationEngine from the [Mapbox SDK](https://github.com/mapbox/mapbox-gl-native/tree/master/platform/android).
- We removed the telemetry class from the project. Nothing is being sent to Mapbox or Maplibre.

## Getting Started

If you are looking to include this inside your project, you have to follow the following steps:

### Gradle

#### Stable Release (Maven Central)
```groovy
repositories {
    google()
    mavenCentral()
}

dependencies {
    // Core navigation logic
    implementation 'io.github.track-asia:navigation-core:2.0.2'

    // Turn-by-turn Navigation UI (Android)
    implementation 'io.github.track-asia:navigation-ui-android:2.0.2'
}
```

> 📚 **Complete Android Guide & Sample Project**: See [TrackAsia Android Document & Samples](https://github.com/track-asia/trackasia-android-document) for complete integration examples with Kotlin DSL and Groovy DSL.

## Getting Help

- **Have a bug to report?** [Open an issue](https://github.com/track-asia/trackasia-navigation-android/issues).
- **Have a feature request?** [Open an issue](https://github.com/track-asia/trackasia-navigation-android/issues/new).

### Sample code

We have included navigation examples to help you get started with the SDK. In order to see the map or calculate a route, use the official TrackAsia Style URL (`https://maps.track-asia.com/styles/v2/streets.json?key=public`).

### Contributing

We welcome feedback, translations, and code contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.




