package com.okbatech.smartevents.feature.events.domain.model

data class Review(
    val id: String,
    val organizerId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val rating: Float,
    val comment: String,
    val createdAt: Long,
)
