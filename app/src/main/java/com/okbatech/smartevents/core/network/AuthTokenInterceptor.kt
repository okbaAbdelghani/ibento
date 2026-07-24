package com.okbatech.smartevents.core.network

import com.okbatech.smartevents.core.datastore.EvenroPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private val UnauthenticatedPaths = listOf("auth/login", "auth/register")

/** Attaches `Authorization: Bearer <token>` to every request except login/register. */
class AuthTokenInterceptor @Inject constructor(
    private val preferences: EvenroPreferences,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (UnauthenticatedPaths.any { path.endsWith(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking { preferences.sessionToken.firstOrNull() }
        val authed = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(authed)
    }
}
