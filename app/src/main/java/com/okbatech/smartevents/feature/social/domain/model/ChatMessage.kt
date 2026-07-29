package com.okbatech.smartevents.feature.social.domain.model

data class ChatMessage(
    val id: String,
    val threadId: String,
    val senderId: String,
    val body: String,
    val sentAt: Long,
    val deliveredAt: Long? = null,
    val readAt: Long? = null,
)

/** Deterministic thread ids so both sides of a conversation land in the same thread. */
object ChatThreads {
    fun direct(userIdA: String, userIdB: String): String {
        val sorted = listOf(userIdA, userIdB).sorted()
        return "dm_${sorted[0]}_${sorted[1]}"
    }

    fun group(eventId: String): String = "event_$eventId"
}
