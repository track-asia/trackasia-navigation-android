package com.trackasia.navigation.android

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}