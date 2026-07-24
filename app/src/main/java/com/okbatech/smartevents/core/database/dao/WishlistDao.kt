package com.okbatech.smartevents.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okbatech.smartevents.core.database.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: WishlistEntity)

    @Delete
    suspend fun delete(entry: WishlistEntity)

    @Query("SELECT eventId FROM wishlist WHERE userId = :userId")
    fun observeEventIds(userId: String): Flow<List<String>>
}
