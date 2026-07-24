package com.okbatech.smartevents.feature.social.domain.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val createdAt: Long,
    val isRead: Boolean,
)
