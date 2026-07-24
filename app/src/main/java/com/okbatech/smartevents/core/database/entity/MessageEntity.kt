package com.okbatech.smartevents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderId: String,
    val body: String,
    val sentAt: Long,
    val isRead: Boolean = false,
)
