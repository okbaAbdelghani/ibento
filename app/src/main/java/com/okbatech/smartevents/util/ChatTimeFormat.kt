package com.okbatech.smartevents.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// coreLibraryDesugaring is already enabled (see app/build.gradle.kts), so java.time is safe
// to use down to minSdk.
private val bubbleTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatBubbleTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(bubbleTimeFormatter)
