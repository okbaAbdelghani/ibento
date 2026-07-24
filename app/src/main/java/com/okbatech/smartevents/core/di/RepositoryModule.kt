package com.okbatech.smartevents.core.di

import com.okbatech.smartevents.feature.auth.data.AuthRepositoryImpl
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import com.okbatech.smartevents.feature.booking.data.BookingRepositoryImpl
import com.okbatech.smartevents.feature.booking.domain.repository.BookingRepository
import com.okbatech.smartevents.feature.events.data.EventRepositoryImpl
import com.okbatech.smartevents.feature.events.data.ReviewRepositoryImpl
import com.okbatech.smartevents.feature.events.data.WishlistRepositoryImpl
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.repository.ReviewRepository
import com.okbatech.smartevents.feature.events.domain.repository.WishlistRepository
import com.okbatech.smartevents.feature.social.data.ChatRepositoryImpl
import com.okbatech.smartevents.feature.social.data.NotificationRepositoryImpl
import com.okbatech.smartevents.feature.social.domain.repository.ChatRepository
import com.okbatech.smartevents.feature.social.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(impl: WishlistRepositoryImpl): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
