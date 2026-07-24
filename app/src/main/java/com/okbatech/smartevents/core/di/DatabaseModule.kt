package com.okbatech.smartevents.core.di

import android.content.Context
import androidx.room.Room
import com.okbatech.smartevents.core.database.AppDatabase
import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.dao.NotificationDao
import com.okbatech.smartevents.core.database.dao.ReviewDao
import com.okbatech.smartevents.core.database.dao.WishlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // Pre-release schema — destructive fallback is fine until the app ships.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideReviewDao(database: AppDatabase): ReviewDao = database.reviewDao()

    @Provides
    fun provideWishlistDao(database: AppDatabase): WishlistDao = database.wishlistDao()
}
