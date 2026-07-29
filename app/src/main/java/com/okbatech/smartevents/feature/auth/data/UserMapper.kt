package com.okbatech.smartevents.feature.auth.data

import com.okbatech.smartevents.core.network.UserDto
import com.okbatech.smartevents.feature.auth.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    coverUrl = coverUrl,
    bio = bio,
    interests = interests,
    city = city,
    country = country,
    followerCount = followerCount,
    followingCount = followingCount,
    lastSeenAt = lastSeenAt,
)
