package com.okbatech.smartevents.feature.auth.domain.model

data class User(
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
