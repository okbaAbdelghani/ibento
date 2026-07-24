package com.okbatech.smartevents.feature.events.presentation.filter

import java.io.Serializable

// Serializable so it can round-trip through NavBackStackEntry.savedStateHandle as a filter result.
data class FilterCriteria(
    val categories: Set<String> = emptySet(),
    val minPrice: Float = 0f,
    val maxPrice: Float = 500f,
) : Serializable

const val FilterResultKey = "filter_result"
