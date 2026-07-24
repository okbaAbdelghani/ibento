package com.okbatech.smartevents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okbatech.smartevents.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY sentAt ASC")
    fun observeByThread(threadId: String): Flow<List<MessageEntity>>
}
