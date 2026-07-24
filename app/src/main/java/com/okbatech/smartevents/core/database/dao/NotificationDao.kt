package com.okbatech.smartevents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okbatech.smartevents.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}
