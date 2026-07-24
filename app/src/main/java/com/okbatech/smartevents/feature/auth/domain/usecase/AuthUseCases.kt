package com.okbatech.smartevents.feature.auth.domain.usecase

import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> = repository.currentUser
}

class ObserveUserByIdUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(userId: String): Flow<User?> = repository.observeUserById(userId)
}

class ObserveOtherUsersUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(excludingUserId: String): Flow<List<User>> = repository.observeOtherUsers(excludingUserId)
}

class ObserveUsersByIdsUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(ids: List<String>): Flow<List<User>> = repository.observeUsersByIds(ids)
}

class ObserveOnboardingStateUseCase @Inject constructor(private val repository: AuthRepository) {
    val hasOnboarded: Flow<Boolean> get() = repository.hasOnboarded
    val isLoggedIn: Flow<Boolean> get() = repository.isLoggedIn
}

class CompleteOnboardingUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.completeOnboarding()
}

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> =
        repository.signUp(name, email, password)
}

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, rememberMe: Boolean): Result<User> =
        repository.signIn(email, password, rememberMe)
}

class VerifyOtpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(code: String): Result<Unit> = repository.verifyOtp(code)
}

class SendPasswordResetUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> = repository.sendPasswordReset(email)
}

class SaveInterestsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(interests: List<String>): Result<Unit> = repository.saveInterests(interests)
}

class SaveLocationUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(city: String, country: String): Result<Unit> =
        repository.saveLocation(city, country)
}

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

class UpdateProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, phone: String?, bio: String?, avatarUrl: String?): Result<Unit> =
        repository.updateProfile(name, phone, bio, avatarUrl)
}
