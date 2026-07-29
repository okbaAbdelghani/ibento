package com.okbatech.smartevents.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseDto

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): AuthResponseDto

    @POST("auth/facebook")
    suspend fun loginWithFacebook(@Body request: FacebookAuthRequest): AuthResponseDto

    @GET("auth/me")
    suspend fun me(): UserDto

    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): UserDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto

    @GET("users")
    suspend fun listUsers(
        @Query("excluding") excluding: String? = null,
        @Query("ids") ids: String? = null,
    ): List<UserDto>

    @GET("events")
    suspend fun listEvents(@Query("organizerId") organizerId: String? = null): List<EventDto>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventDto

    @GET("events/{id}/attendees")
    suspend fun getAttendees(@Path("id") id: String): List<String>

    @POST("events")
    suspend fun createEvent(@Body request: EventInputRequest): EventDto

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body request: EventInputRequest): EventDto

    @GET("bookings/me")
    suspend fun myBookings(): List<BookingDto>

    @GET("bookings/{id}")
    suspend fun getBooking(@Path("id") id: String): BookingDto

    @POST("bookings")
    suspend fun createBooking(@Body request: BookEventRequest): BookingDto

    @POST("devices")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest)

    @POST("push/notify")
    suspend fun notifyPush(@Body request: PushNotifyRequest)

    @POST("users/me/heartbeat")
    suspend fun sendHeartbeat()

    @GET("turn/credentials")
    suspend fun getTurnCredentials(): TurnCredentialsDto

    @POST("calls/notify")
    suspend fun notifyCall(@Body request: CallNotifyRequest)
}
