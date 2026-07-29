package com.okbatech.smartevents.feature.auth.data

import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.DeviceTokenRequest
import com.okbatech.smartevents.core.network.FacebookAuthRequest
import com.okbatech.smartevents.core.network.GoogleAuthRequest
import com.okbatech.smartevents.core.network.LoginRequest
import com.okbatech.smartevents.core.network.RegisterRequest
import com.okbatech.smartevents.core.network.UpdateProfileRequest
import com.okbatech.smartevents.core.network.safeApiCall
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen
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
            // api.me() is a single network round trip on the same real mobile connection chat
            // delivery depends on (Wi-Fi/cellular handoff, brief drops) — a bare one-shot call
            // here used to leave currentUserId null for the rest of the screen's lifetime on
            // any transient failure, silently breaking both message send (which no-ops without
            // a user id) and sent/received bubble alignment. Retry a few times with backoff
            // before giving up.
            flow<User?> { emit(api.me().toDomain()) }
                .retryWhen { _, attempt -> (attempt < 3).also { if (it) delay(500L * (attempt + 1)) } }
                .catch { emit(null) }
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

    override suspend fun signInWithGoogle(idToken: String): Result<User> =
        safeApiCall { api.loginWithGoogle(GoogleAuthRequest(idToken)) }
            .onSuccess { response -> preferences.setSession(response.user.id, response.token) }
            .map { it.user.toDomain() }

    override suspend fun signInWithFacebook(accessToken: String): Result<User> =
        safeApiCall { api.loginWithFacebook(FacebookAuthRequest(accessToken)) }
            .onSuccess { response -> preferences.setSession(response.user.id, response.token) }
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

    override suspend fun registerDeviceToken(token: String): Result<Unit> =
        safeApiCall { api.registerDeviceToken(DeviceTokenRequest(token)) }

    override suspend fun sendHeartbeat(): Result<Unit> =
        safeApiCall { api.sendHeartbeat() }
}
