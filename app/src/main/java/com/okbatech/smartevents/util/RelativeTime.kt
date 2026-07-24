package com.okbatech.smartevents.util

import java.util.concurrent.TimeUnit

fun formatRelativeTime(epochMillis: Long): String {
    val diff = System.currentTimeMillis() - epochMillis
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> formatEventDate(epochMillis)
    }
}
