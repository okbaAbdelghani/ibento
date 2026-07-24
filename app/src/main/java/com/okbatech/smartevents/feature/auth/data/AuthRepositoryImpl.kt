package com.okbatech.smartevents.feature.auth.data

import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.LoginRequest
import com.okbatech.smartevents.core.network.RegisterRequest
import com.okbatech.smartevents.core.network.UpdateProfileRequest
import com.okbatech.smartevents.core.network.safeApiCall
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val preferences: EvenroPreferences,
) : AuthRepository {

    override val currentUser: Flow<User?> = preferences.currentUserId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            flow { emit(runCatching { api.me() }.getOrNull()?.toDomain()) }
        }
    }

    override val isLoggedIn: Flow<Boolean> = preferences.isLoggedIn
    override val hasOnboarded: Flow<Boolean> = preferences.hasOnboarded

    override fun observeUserById(userId: String): Flow<User?> =
        flow { emit(runCatching { api.getUser(userId) }.getOrNull()?.toDomain()) }

    override fun observeOtherUsers(excludingUserId: String): Flow<List<User>> = flow {
        emit(runCatching { api.listUsers(excluding = excludingUserId) }.getOrDefault(emptyList()).map { it.toDomain() })
    }

    override fun observeUsersByIds(ids: List<String>): Flow<List<User>> = flow {
        if (ids.isEmpty()) {
            emit(emptyList())
        } else {
            emit(runCatching { api.listUsers(ids = ids.joinToString(",")) }.getOrDefault(emptyList()).map { it.toDomain() })
        }
    }

    override suspend fun completeOnboarding() = preferences.setOnboardingComplete()

    override suspend fun signUp(name: String, email: String, password: String): Result<User> =
        safeApiCall { api.register(RegisterRequest(name, email, password)) }
            .onSuccess { response -> preferences.setSession(response.user.id, response.token) }
            .map { it.user.toDomain() }

    override suspend fun signIn(email: String, password: String, rememberMe: Boolean): Result<User> =
        safeApiCall { api.login(LoginRequest(email, password)) }
            .onSuccess { response -> preferences.setSession(response.user.id, response.token, rememberMe) }
            .map { it.user.toDomain() }

    override suspend fun verifyOtp(code: String): Result<Unit> {
        // No SMS/email provider behind this build — any non-blank code is accepted.
        return if (code.isNotBlank()) Result.success(Unit) else Result.failure(IllegalArgumentException("Enter the code"))
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        // No real email provider behind this build — always reports success.
        return Result.success(Unit)
    }

    override suspend fun saveInterests(interests: List<String>): Result<Unit> =
        safeApiCall { api.updateMe(UpdateProfileRequest(interests = interests)) }.map { }

    override suspend fun saveLocation(city: String, country: String): Result<Unit> =
        safeApiCall { api.updateMe(UpdateProfileRequest(city = city, country = country)) }.map { }

    override suspend fun updateProfile(name: String, phone: String?, bio: String?, avatarUrl: String?): Result<Unit> =
        safeApiCall {
            api.updateMe(UpdateProfileRequest(name = name, phone = phone, bio = bio, avatarUrl = avatarUrl))
        }.map { }

    override suspend fun signOut() {
        preferences.setSession(null, null)
    }
}
