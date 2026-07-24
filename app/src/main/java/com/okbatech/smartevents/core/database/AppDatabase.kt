package com.okbatech.smartevents.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.dao.NotificationDao
import com.okbatech.smartevents.core.database.dao.ReviewDao
import com.okbatech.smartevents.core.database.dao.WishlistDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.database.entity.NotificationEntity
import com.okbatech.smartevents.core.database.entity.ReviewEntity
import com.okbatech.smartevents.core.database.entity.WishlistEntity

/**
 * Users, Events, and Bookings moved to the backend (see `backend/`) — this database now
 * only holds the features still local-only for this phase: reviews, notifications, chat
 * messages, and wishlist toggles.
 */
@Database(
    entities = [
        MessageEntity::class,
        NotificationEntity::class,
        ReviewEntity::class,
        WishlistEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reviewDao(): ReviewDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        const val DATABASE_NAME = "smartevents.db"
    }
}
