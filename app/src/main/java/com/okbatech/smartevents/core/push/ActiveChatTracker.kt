package com.okbatech.smartevents.core.push

/**
 * Tracks which chat thread (if any) is currently on screen, so an incoming push notification
 * for that exact thread can be suppressed — the user is already looking at the message live.
 * Set/cleared from `ChatViewModel`'s init/onCleared.
 */
object ActiveChatTracker {
    @Volatile var openThreadId: String? = null
}
