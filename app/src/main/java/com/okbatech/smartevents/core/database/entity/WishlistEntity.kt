package com.okbatech.smartevents.core.database.entity

import androidx.room.Entity

@Entity(tableName = "wishlist", primaryKeys = ["userId", "eventId"])
data class WishlistEntity(
    val userId: String,
    val eventId: String,
    val addedAt: Long,
)
