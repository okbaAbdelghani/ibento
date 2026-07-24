package com.okbatech.smartevents.core.network

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val interests: List<String> = emptyList(),
    val city: String? = null,
    val country: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
)

@Serializable
data class AuthResponseDto(val token: String, val user: UserDto)

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val startDateTime: Long,
    val endDateTime: Long,
    val venueName: String,
    val city: String,
    val priceAmount: Double,
    val currency: String,
    val organizerId: String,
    val capacity: Int,
    val attendeeCount: Int,
    val isFeatured: Boolean,
)

@Serializable
data class BookingDto(
    val id: String,
    val eventId: String,
    val userId: String,
    val ticketCount: Int,
    val totalPrice: Double,
    val status: String,
    val bookedAt: Long,
    val qrCode: String,
)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val city: String? = null,
    val country: String? = null,
    val interests: List<String>? = null,
)

@Serializable
data class EventInputRequest(
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val startDateTime: Long,
    val endDateTime: Long,
    val venueName: String,
    val city: String,
    val priceAmount: Double,
    val capacity: Int,
)

@Serializable
data class BookEventRequest(val eventId: String, val ticketCount: Int)

@Serializable
data class ApiErrorBody(val message: String? = null)
