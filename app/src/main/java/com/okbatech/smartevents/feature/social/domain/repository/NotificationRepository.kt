package com.okbatech.smartevents.feature.social.domain.repository

import com.okbatech.smartevents.feature.social.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
}
