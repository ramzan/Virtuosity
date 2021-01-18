package com.nazmar.musicgym.practice.session

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.nazmar.musicgym.R
import com.nazmar.musicgym.TIMER_NOTIFICATION_ID
import com.nazmar.musicgym.getTimerNotificationBuilder
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

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, sound)
        notificationManager = application.getSystemService(NotificationManager::class.java)

        val restartPendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(RESTART_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                Intent(PAUSE_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val resumePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                2,
                Intent(RESUME_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val restartAction = NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_replay_24,
                getString(R.string.restart_timer),
                restartPendingIntent).build()
        val pauseAction = NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_pause_24,
                getString(R.string.pause_timer),
                pausePendingIntent).build()
        val resumeAction = NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_play_arrow_24,
                getString(R.string.start_timer),
                resumePendingIntent).build()

        runningNotification = getTimerNotificationBuilder(this, pauseAction, restartAction)

        pausedNotification = getTimerNotificationBuilder(this, resumeAction, restartAction)

        stoppedNotification = getTimerNotificationBuilder(this, null, null)

        timer = Timer(runningNotification, pausedNotification, stoppedNotification, notificationManager, mediaPlayer)
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
}