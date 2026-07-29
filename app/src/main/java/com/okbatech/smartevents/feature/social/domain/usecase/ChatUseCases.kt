package com.okbatech.smartevents.feature.social.domain.usecase

import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(private val repository: ChatRepository) {
    operator fun invoke(threadId: String): Flow<List<ChatMessage>> = repository.observeMessages(threadId)
}

class SendMessageUseCase @Inject constructor(private val repository: ChatRepository) {
    suspend operator fun invoke(threadId: String, senderId: String, body: String) =
        repository.sendMessage(threadId, senderId, body)
}

class ObserveLastMessageUseCase @Inject constructor(private val repository: ChatRepository) {
    operator fun invoke(threadId: String): Flow<ChatMessage?> = repository.observeLastMessage(threadId)
}

class ObserveThreadUnreadCountUseCase @Inject constructor(private val repository: ChatRepository) {
    operator fun invoke(threadId: String, myUserId: String): Flow<Int> =
        repository.observeThreadUnreadCount(threadId, myUserId)
}

class ObserveTotalUnreadCountUseCase @Inject constructor(private val repository: ChatRepository) {
    operator fun invoke(myUserId: String): Flow<Int> = repository.observeTotalUnreadCount(myUserId)
}

class MarkThreadSeenUseCase @Inject constructor(private val repository: ChatRepository) {
    suspend operator fun invoke(threadId: String, myUserId: String) = repository.markThreadSeen(threadId, myUserId)
}
