package com.okbatech.smartevents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okbatech.smartevents.core.database.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE organizerId = :organizerId ORDER BY createdAt DESC")
    fun observeByOrganizer(organizerId: String): Flow<List<ReviewEntity>>
}
