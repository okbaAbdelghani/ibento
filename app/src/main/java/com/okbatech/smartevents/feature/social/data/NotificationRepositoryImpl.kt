package com.okbatech.smartevents.feature.social.data

import com.okbatech.smartevents.core.database.dao.NotificationDao
import com.okbatech.smartevents.core.database.entity.NotificationEntity
import com.okbatech.smartevents.feature.social.domain.model.AppNotification
import com.okbatech.smartevents.feature.social.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        notificationDao.observeByUser(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun markAsRead(notificationId: String) = notificationDao.markAsRead(notificationId)

    override suspend fun markAllAsRead(userId: String) = notificationDao.markAllAsRead(userId)
}

private fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    title = title,
    body = body,
    type = type,
    createdAt = createdAt,
    isRead = isRead,
)
