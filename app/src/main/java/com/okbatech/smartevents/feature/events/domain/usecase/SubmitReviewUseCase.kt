package com.okbatech.smartevents.feature.events.domain.usecase

import com.okbatech.smartevents.feature.events.domain.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(private val repository: ReviewRepository) {
    suspend operator fun invoke(organizerId: String, authorId: String, rating: Float, comment: String): Result<Unit> =
        repository.submitReview(organizerId, authorId, rating, comment)
}
