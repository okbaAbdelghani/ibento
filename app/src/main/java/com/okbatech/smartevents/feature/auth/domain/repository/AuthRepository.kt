package com.okbatech.smartevents.feature.auth.domain.repository

import com.okbatech.smartevents.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Flow<Boolean>
    val hasOnboarded: Flow<Boolean>

    fun observeUserById(userId: String): Flow<User?>
    fun observeOtherUsers(excludingUserId: String): Flow<List<User>>
    fun observeUsersByIds(ids: List<String>): Flow<List<User>>

    suspend fun completeOnboarding()

    suspend fun signUp(name: String, email: String, password: String): Result<User>
    suspend fun signIn(email: String, password: String, rememberMe: Boolean): Result<User>

    /** Mock verification — there's no real SMS/email provider behind this build. */
    suspend fun verifyOtp(code: String): Result<Unit>

    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun saveInterests(interests: List<String>): Result<Unit>
    suspend fun saveLocation(city: String, country: String): Result<Unit>
    suspend fun updateProfile(name: String, phone: String?, bio: String?, avatarUrl: String?): Result<Unit>
    suspend fun signOut()
}
