package com.nazmar.musicgym.practice.session.timer

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.nazmar.musicgym.*
import java.util.*


const val RESUME_TIMER = "resume_timer"
const val PAUSE_TIMER = "pause_timer"
const val RESTART_TIMER = "restart_timer"

class TimerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var notificationManager: NotificationManager

    private lateinit var timer: Timer

    private lateinit var timerReceiver: TimerReceiver

    private lateinit var pausedNotification: NotificationCompat.Builder

    private lateinit var runningNotification: NotificationCompat.Builder

    private lateinit var stoppedNotification: NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer.create(this, R.raw.bell)
        notificationManager = application.getSystemService(NotificationManager::class.java)

        runningNotification = getTimerNotificationBuilder(TimerState.RUNNING)
        pausedNotification = getTimerNotificationBuilder(TimerState.PAUSED)
        stoppedNotification = getTimerNotificationBuilder(TimerState.STOPPED)

        val vibrator =
            PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_timer_vibrate), true)
                .let {
                    if (it) getSystemService(Vibrator::class.java) else null
                }

        timer = Timer(
            runningNotification,
            pausedNotification,
            stoppedNotification,
            notificationManager,
            getString(R.string.timer_notification_time_remaining_prefix),
            getString(R.string.timer_notification_time_up_message),
            mediaPlayer,
            vibrator
        )
        timerReceiver = TimerReceiver(timer)
        IntentFilter().apply {
            addAction(RESUME_TIMER)
            addAction(PAUSE_TIMER)
            addAction(RESTART_TIMER)
        }.also {
            registerReceiver(timerReceiver, it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(TIMER_NOTIFICATION_ID, stoppedNotification.build())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timerReceiver)
        timer.clearTimer()
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = TimerBinder()

    inner class TimerBinder : Binder() {
        fun getTimer(): Timer = this@TimerService.timer
        fun updateRoutineName(name: String) {
            runningNotification.setSubText(name)
            pausedNotification.setSubText(name)
            stoppedNotification.setSubText(name)
        }
    }

    private fun getTimerNotificationBuilder(
        timerState: TimerState
    ): NotificationCompat.Builder {

        val contentPendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            TIMER_NOTIFICATION_ID,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            this,
            getString(R.string.timer_notification_channel_id)
        )
            .setTicker(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setContentText(getString(R.string.timer_notification_stopped_message))

        if (timerState != TimerState.STOPPED) {

            val playAction = if (timerState == TimerState.PAUSED) {
                PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE_RESUME,
                    Intent(RESUME_TIMER),
                    PendingIntent.FLAG_UPDATE_CURRENT
                ).let { resumePendingIntent ->
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_baseline_play_arrow_24,
                        getString(R.string.start_timer),
                        resumePendingIntent
                    ).build()
                }
            } else {
                PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE_PAUSE,
                    Intent(PAUSE_TIMER),
                    PendingIntent.FLAG_UPDATE_CURRENT
                ).let { pausePendingIntent ->
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_baseline_pause_24,
                        getString(R.string.pause_timer),
                        pausePendingIntent
                    ).build()
                }
            }

            val restartAction = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_RESTART,
                Intent(RESTART_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
            ).let { restartPendingIntent ->
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_replay_24,
                    getString(R.string.restart_timer),
                    restartPendingIntent
                ).build()
            }

            builder.apply {
                addAction(playAction)
                addAction(restartAction)
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1)
                )
            }
        }
        return builder
    }
}