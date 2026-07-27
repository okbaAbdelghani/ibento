package com.okbatech.smartevents.feature.events.domain.model

data class EventsLoadState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
