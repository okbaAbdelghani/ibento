package com.okbatech.smartevents.feature.social.domain.usecase

import com.okbatech.smartevents.feature.social.domain.model.AppNotification
import com.okbatech.smartevents.feature.social.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotificationsUseCase @Inject constructor(private val repository: NotificationRepository) {
    operator fun invoke(userId: String): Flow<List<AppNotification>> = repository.observeNotifications(userId)
}

class MarkNotificationReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.markAsRead(notificationId)
}

class MarkAllNotificationsReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(userId: String) = repository.markAllAsRead(userId)
}
