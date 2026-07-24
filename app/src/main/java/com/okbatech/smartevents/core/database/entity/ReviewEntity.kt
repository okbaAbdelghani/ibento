package com.okbatech.smartevents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val organizerId: String,
    val authorId: String,
    val rating: Float,
    val comment: String,
    val createdAt: Long,
)
