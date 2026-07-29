package com.okbatech.smartevents

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.okbatech.smartevents.core.push.EXTRA_THREAD_ID
import com.okbatech.smartevents.core.push.EXTRA_THREAD_TITLE
import com.okbatech.smartevents.core.navigation.EvenroNavHost
import com.okbatech.smartevents.core.navigation.PendingChatDeepLink
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SmartEventsTheme {
                EvenroNavHost(pendingChatDeepLink = intent.toPendingChatDeepLink())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Simplest way to feed a new deep link into the Compose tree set up in onCreate above —
        // this activity has no other mutable state worth preserving across the recreation.
        setIntent(intent)
        recreate()
    }

    private fun Intent.toPendingChatDeepLink(): PendingChatDeepLink? {
        val threadId = getStringExtra(EXTRA_THREAD_ID) ?: return null
        val title = getStringExtra(EXTRA_THREAD_TITLE) ?: return null
        return PendingChatDeepLink(threadId, title)
    }
}
