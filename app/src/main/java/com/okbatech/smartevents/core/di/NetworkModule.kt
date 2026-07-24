package com.okbatech.smartevents.core.di

import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.AuthTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * `10.0.2.2` is the Android emulator's alias for the host machine's loopback interface —
 * `localhost` from inside the AVD would resolve to the emulator itself, not the dev backend.
 */
private const val BASE_URL = "http://10.0.2.2:4000/api/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenInterceptor: AuthTokenInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
