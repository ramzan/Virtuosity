package com.nazmar.musicgym.practice.session

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.R
import com.nazmar.musicgym.TimerState

const val TIMER_NOTIFICATION_ID = 0

class TimerService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var notificationManager: NotificationManager

    private lateinit var notification: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, sound)
        notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification = NotificationCompat.Builder(this, application.getString(R.string.timer_notification_channel_id))
                .setTicker(application.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setOngoing(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(TIMER_NOTIFICATION_ID, notification.build())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.clearTimer()
        notificationManager.cancelAll()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = TimerBinder()

    private val timer = Timer()

    inner class TimerBinder : Binder() {
        fun getTimer(): Timer = this@TimerService.timer
    }

    inner class Timer {

        private fun updateTimerNotification() {
            notification.setContentTitle(currentExerciseName)
            notificationManager.notify(TIMER_NOTIFICATION_ID, notification.build())
        }

        private var timer: CountDownTimer? = null

        private var _timeLeft = MutableLiveData<Long>(null)

        val timeLeft: LiveData<Long>
            get() = _timeLeft

        val timeString = Transformations.map(timeLeft) { time ->
            timeToString((time ?: 0L))
        }

        private var _timerStatus = MutableLiveData(TimerState.STOPPED)

        val timerStatus: LiveData<TimerState>
            get() = _timerStatus

        private var currentExerciseDuration = 0L

        private var currentExerciseName = ""

        private var exerciseIndex = -1

        private fun timeToString(time: Long): String {
            return "${time / 60000}:" + "${(time / 1000) % 60}".padStart(2, '0')
        }

        private fun createTimer() {
            timer = object : CountDownTimer(timeLeft.value ?: currentExerciseDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timeLeft.value = millisUntilFinished
                    notification.setContentText("Time remaining: ${timeToString(millisUntilFinished)}")
                    updateTimerNotification()
                }

                override fun onFinish() {
                    timer = null
                    _timeLeft.value = null
                    notification.setContentText("Time's up!")
                    updateTimerNotification()
                    mediaPlayer.start()
                    _timerStatus.value = TimerState.COMPLETED
                }
            }
            _timeLeft.value = timeLeft.value ?: currentExerciseDuration
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
                it.start()
                _timerStatus.value = TimerState.RUNNING
            }
        }

        fun pauseTimer() {
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
        }

        fun updateTimeLeft(newTime: Long) {
            timer?.cancel()
            _timeLeft.value = newTime
            if (newTime != 0L) createTimer()
        }
    }
}