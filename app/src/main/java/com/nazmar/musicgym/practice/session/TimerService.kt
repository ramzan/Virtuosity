package com.nazmar.musicgym.practice.session

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.R
import com.nazmar.musicgym.TIMER_NOTIFICATION_ID
import com.nazmar.musicgym.TimerState
import com.nazmar.musicgym.getTimerNotificationBuilder
import java.text.SimpleDateFormat
import java.util.*


const val RESUME_TIMER = "resume_timer"
const val PAUSE_TIMER = "pause_timer"
const val RESTART_TIMER = "restart_timer"

class TimerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var notificationManager: NotificationManager

    private lateinit var timer: TimerService.Timer

    private lateinit var timerReceiver: TimerReceiver

    private lateinit var pausedNotification: NotificationCompat.Builder

    private lateinit var runningNotification: NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, sound)
        notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        timer = Timer()
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
        startForeground(TIMER_NOTIFICATION_ID, runningNotification.build())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timerReceiver)
        timer.clearTimer()
        notificationManager.cancelAll()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        unregisterReceiver(timerReceiver)
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
        }
    }

    inner class Timer {

        private var notification = runningNotification

        private val timeFormatter = SimpleDateFormat("mm:ss", Locale.US)

        private fun updateTimerNotification() {
            notification.setContentTitle(currentExerciseName)
            notification.setContentText("Time remaining: ${timeString.value}")
            notificationManager.notify(TIMER_NOTIFICATION_ID, notification.build())
        }

        private fun showTimeUpNotification() {
            notification.setContentTitle(currentExerciseName)
            notification.setContentText("Time's up!")
            notificationManager.notify(TIMER_NOTIFICATION_ID, notification.build())
        }

        private var timer: CountDownTimer? = null

        private var _timeLeft = MutableLiveData<Long>(null)

        val timeLeft: LiveData<Long>
            get() = _timeLeft

        private val _timeString = MutableLiveData("")

        val timeString: LiveData<String>
            get() = _timeString

        private var _timerStatus = MutableLiveData(TimerState.STOPPED)

        val timerStatus: LiveData<TimerState>
            get() = _timerStatus

        private var currentExerciseDuration = 0L

        private var currentExerciseName = ""

        private var exerciseIndex = -1

        private fun timeToString(time: Long): String {
            return timeFormatter.format(time)
        }

        private fun createTimer() {
            with(timeLeft.value ?: currentExerciseDuration) {
                timer = object : CountDownTimer(this, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        _timeLeft.value = millisUntilFinished
                        _timeString.value = timeToString(millisUntilFinished)
                        updateTimerNotification()
                    }

                    override fun onFinish() {
                        timer = null
                        _timeLeft.value = null
                        notification = pausedNotification
                        showTimeUpNotification()
                        mediaPlayer.start()
                        _timerStatus.value = TimerState.COMPLETED
                    }
                }
                _timeString.value = timeToString(this)
                _timeLeft.value = this
            }
            updateTimerNotification()
        }

        fun setUpTimer(newIndex: Int, newExerciseDuration: Long, newExerciseName: String) {
            if (newIndex != exerciseIndex) {
                currentExerciseName = newExerciseName
                exerciseIndex = newIndex
                currentExerciseDuration = newExerciseDuration
                clearTimer()
                if (currentExerciseDuration != 0L) {
                    createTimer()
                    startTimer()
                }
            }
        }

        fun startTimer() {
            timer?.let {
                notification = runningNotification
                it.start()
                _timerStatus.value = TimerState.RUNNING
            }
        }

        fun pauseTimer() {
            notification = pausedNotification
            timer?.cancel()
            createTimer()
            _timerStatus.value = TimerState.PAUSED
        }

        fun restartTimer() {
            timer?.cancel()
            _timeLeft.value = null
            if (currentExerciseDuration != 0L) {
                createTimer()
                if (timerStatus.value == TimerState.RUNNING) {
                    startTimer()
                } else {
                    _timerStatus.value = TimerState.PAUSED
                }
            } else {
                _timerStatus.value = TimerState.STOPPED
            }
        }

        fun clearTimer() {
            timer?.cancel()
            timer = null
            _timerStatus.value = TimerState.STOPPED
            _timeLeft.value = null
            _timeString.value = timeToString(0)
            notification = pausedNotification
            updateTimerNotification()
        }

        fun updateTimeLeft(newTime: Long) {
            timer?.cancel()
            _timeLeft.value = newTime
            if (newTime != 0L) createTimer()
        }
    }
}