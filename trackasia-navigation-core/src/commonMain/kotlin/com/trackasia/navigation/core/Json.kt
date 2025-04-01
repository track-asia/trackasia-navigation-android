package com.trackasia.navigation.core

import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

val json = Json {
    // Encode
    encodeDefaults = true
    explicitNulls = false

    // Decode
    ignoreUnknownKeys = true
    isLenient = true
}