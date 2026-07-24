package com.okbatech.smartevents.core.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private val ErrorJson = Json { ignoreUnknownKeys = true }

/** Runs a suspend Retrofit call, turning HTTP/network failures into a readable [Result]. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val message = e.response()?.errorBody()?.string()
        ?.let { body -> runCatching { ErrorJson.decodeFromString<ApiErrorBody>(body).message }.getOrNull() }
        ?: e.message()
    Result.failure(IllegalStateException(message ?: "Request failed (${e.code()})"))
} catch (e: IOException) {
    Result.failure(IllegalStateException("Network error — check your connection.", e))
}
