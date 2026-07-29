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

    @Query("UPDATE messages SET deliveredAt = :deliveredAt WHERE id = :id AND deliveredAt IS NULL")
    suspend fun markDelivered(id: String, deliveredAt: Long)

    /** Flips readAt ("blue tick") on every message *I* sent in [threadId] up to [upToSentAt]. */
    @Query(
        "UPDATE messages SET readAt = :readAt " +
            "WHERE threadId = :threadId AND senderId = :senderId AND sentAt <= :upToSentAt AND readAt IS NULL",
    )
    suspend fun markReadUpTo(threadId: String, senderId: String, upToSentAt: Long, readAt: Long)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY sentAt DESC LIMIT 1")
    fun observeLastMessage(threadId: String): Flow<MessageEntity?>

    /** Unread messages *sent to me* (anything not from [myUserId]) within one thread. */
    @Query("SELECT COUNT(*) FROM messages WHERE threadId = :threadId AND senderId != :myUserId AND seenAt IS NULL")
    fun observeThreadUnreadCount(threadId: String, myUserId: String): Flow<Int>

    /** Unread messages *sent to me* across every thread — backs the home header badge. */
    @Query("SELECT COUNT(*) FROM messages WHERE senderId != :myUserId AND seenAt IS NULL")
    fun observeTotalUnreadCount(myUserId: String): Flow<Int>

    /** Marks every unseen incoming message in [threadId] as seen — clears its unread badge. */
    @Query("UPDATE messages SET seenAt = :seenAt WHERE threadId = :threadId AND senderId != :myUserId AND seenAt IS NULL")
    suspend fun markThreadSeen(threadId: String, myUserId: String, seenAt: Long)
}
