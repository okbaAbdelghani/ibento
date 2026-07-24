package com.okbatech.smartevents.core.database

import com.okbatech.smartevents.core.database.entity.ReviewEntity
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Seeds the local, still-mocked features (reviews) on first launch. Users, events, and
 * bookings are now seeded server-side (see `backend/prisma/seed.ts`) with the same ids,
 * so this only needs to cover what's still local.
 */
class DatabaseSeeder @Inject constructor(
    private val database: AppDatabase,
    private val preferences: EvenroPreferences,
) {
    suspend fun seedIfNeeded() {
        if (preferences.hasSeededDatabase()) return

        val now = System.currentTimeMillis()
        val day = TimeUnit.DAYS.toMillis(1)

        val reviews = listOf(
            ReviewEntity(
                id = "r-1",
                organizerId = "u-tamim",
                authorId = "u-rafi",
                rating = 5f,
                comment = "Elementum convallis praesent scelerisque fringilla at fermentum, fames. Nunc metus, mattis non ac. Quis convallis fringilla.",
                createdAt = now - 6 * day,
            ),
            ReviewEntity(
                id = "r-2",
                organizerId = "u-tamim",
                authorId = "u-rafi",
                rating = 5f,
                comment = "Vitae lacus, at placerat non sapien. Urna, eu non id nec. Est eu, ut adipiscing blandit enim tempus lectus estonjabit phasellus.",
                createdAt = now - 14 * day,
            ),
        )
        database.reviewDao().insertAll(reviews)

        preferences.markDatabaseSeeded()
    }
}
