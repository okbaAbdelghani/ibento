package com.okbatech.smartevents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val createdAt: Long,
    val isRead: Boolean = false,
)
