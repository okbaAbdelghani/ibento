package com.okbatech.smartevents.feature.events.presentation.filter

import java.io.Serializable

const val AllCategory = "All"

// Serializable so it can round-trip through NavBackStackEntry.savedStateHandle as a filter result.
data class FilterCriteria(
    val category: String = AllCategory,
    val minPrice: Float = 0f,
    val maxPrice: Float = 500f,
) : Serializable

const val FilterResultKey = "filter_result"
const val CurrentFilterKey = "current_filter"
