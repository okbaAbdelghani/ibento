package com.okbatech.smartevents.feature.events.data

import com.okbatech.smartevents.core.database.dao.ReviewDao
import com.okbatech.smartevents.core.database.entity.ReviewEntity
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.feature.events.domain.model.Review
import com.okbatech.smartevents.feature.events.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
    private val api: ApiService,
) : ReviewRepository {

    override fun observeReviewsByOrganizer(organizerId: String): Flow<List<Review>> =
        reviewDao.observeByOrganizer(organizerId).map { reviews ->
            reviews.map { review ->
                val author = runCatching { api.getUser(review.authorId) }.getOrNull()
                Review(
                    id = review.id,
                    organizerId = review.organizerId,
                    authorId = review.authorId,
                    authorName = author?.name ?: "Evenro user",
                    authorAvatarUrl = author?.avatarUrl,
                    rating = review.rating,
                    comment = review.comment,
                    createdAt = review.createdAt,
                )
            }
        }

    override suspend fun submitReview(organizerId: String, authorId: String, rating: Float, comment: String): Result<Unit> {
        reviewDao.insertAll(
            listOf(
                ReviewEntity(
                    id = "r-${UUID.randomUUID()}",
                    organizerId = organizerId,
                    authorId = authorId,
                    rating = rating,
                    comment = comment,
                    createdAt = System.currentTimeMillis(),
                ),
            ),
        )
        return Result.success(Unit)
    }
}
