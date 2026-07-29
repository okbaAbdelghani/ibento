package com.okbatech.smartevents.feature.call.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.okbatech.smartevents.core.webrtc.EXTRA_CALL_ID
import com.okbatech.smartevents.core.webrtc.EXTRA_IS_INCOMING
import com.okbatech.smartevents.core.webrtc.EXTRA_OTHER_NAME
import com.okbatech.smartevents.core.webrtc.EXTRA_OTHER_USER_ID
import com.okbatech.smartevents.core.webrtc.EXTRA_VIDEO
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import dagger.hilt.android.AndroidEntryPoint

/** Standalone — deliberately NOT part of EvenroNavHost/MainActivity's nav graph. A ringing call
 * needs to show over the lock screen independent of MainActivity's task/back-stack state, and
 * MainActivity.onNewIntent's full recreate() would be far too disruptive here. Launched either
 * from ChatScreen's call buttons (outgoing), CallForegroundService's full-screen-intent
 * (incoming), or directly by CallManager when the app is already foregrounded. */
@AndroidEntryPoint
class CallActivity : ComponentActivity() {

    private val requestCallPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        // callId is unused directly here (CallManager already tracks it via callState) — reading
        // it just documents intent and keeps the extras contract obvious at the call site.
        intent.getStringExtra(EXTRA_CALL_ID)
        val otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID)
        val otherName = intent.getStringExtra(EXTRA_OTHER_NAME)
        val video = intent.getBooleanExtra(EXTRA_VIDEO, false)
        val isIncoming = intent.getBooleanExtra(EXTRA_IS_INCOMING, false)

        requestCallPermissions.launch(requiredPermissions(video))

        setContent {
            SmartEventsTheme {
                CallRoute(
                    otherUserId = otherUserId,
                    otherNameFallback = otherName,
                    video = video,
                    isIncoming = isIncoming,
                    onFinish = { finish() },
                )
            }
        }
    }

    private fun requiredPermissions(video: Boolean): Array<String> {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (video) permissions += Manifest.permission.CAMERA
        return permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }
}
