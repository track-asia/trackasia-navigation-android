# Consumer proguard rules for libandroid-navigation-ui

# --- Picasso ---
-dontwarn com.squareup.okhttp.**

# --- com.mapbox.api.directions.v5.MapboxDirections ---
-dontwarn com.sun.xml.internal.ws.spi.db.BindingContextFactory

# --- com.amazonaws.util.json.JacksonFactory ---
-dontwarn com.fasterxml.jackson.core.**

# --- TrackAsia ---
-dontwarn com.trackasia.navigation.android.ui.v5.**