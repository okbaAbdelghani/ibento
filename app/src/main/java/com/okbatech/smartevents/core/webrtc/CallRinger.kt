package com.okbatech.smartevents.core.webrtc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CallRinger"
private const val RINGBACK_VOLUME_PERCENT = 60

// Waits 0ms, vibrates 1000ms, pauses 1000ms, then repeats from index 0 — a standard incoming-call
// buzz pattern rather than one long continuous buzz.
private val RING_VIBRATION_PATTERN = longArrayOf(0, 1000, 1000)

/**
 * Owns the audible/haptic side of ringing — a phone's incoming-call ring (looped ringtone +
 * repeating vibration) and an outgoing call's ringback ("beep... beep...") tone. Driven purely by
 * [CallManager.callState] transitions (see [CallManager.start]), so it stays in sync with
 * whatever raised the call regardless of entry point (direct start, XMPP offer, FCM push).
 */
@Singleton
class CallRinger @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var ringtonePlayer: MediaPlayer? = null
    private var ringbackTone: ToneGenerator? = null

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /** Incoming call: loops the device's ringtone (respecting DND/ringer-mode routing via
     * USAGE_NOTIFICATION_RINGTONE) plus a repeating vibration, skipped outright in silent mode —
     * matches how the platform Dialer itself decides whether to buzz. */
    fun startRinging() {
        stop()
        runCatching {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtonePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
        }.onFailure { Log.e(TAG, "startRinging: ringtone playback failed", it) }

        if (audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT) {
            runCatching {
                val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(RING_VIBRATION_PATTERN, 0)
                } else {
                    null
                }
                if (effect != null) {
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(RING_VIBRATION_PATTERN, 0)
                }
            }.onFailure { Log.e(TAG, "startRinging: vibration failed", it) }
        }
    }

    /** Outgoing call: loops the standard telecom supervisory ringback tone while waiting for the
     * callee to answer — the "beep... beep..." a caller hears on any normal phone call. */
    fun startRingback() {
        stop()
        runCatching {
            ringbackTone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, RINGBACK_VOLUME_PERCENT).apply {
                startTone(ToneGenerator.TONE_SUP_RINGTONE, Int.MAX_VALUE)
            }
        }.onFailure { Log.e(TAG, "startRingback failed", it) }
    }

    fun stop() {
        ringtonePlayer?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        ringtonePlayer = null
        ringbackTone?.let {
            runCatching { it.stopTone() }
            runCatching { it.release() }
        }
        ringbackTone = null
        runCatching { vibrator.cancel() }
    }
}
