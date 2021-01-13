package com.nazmar.musicgym.practice.session

import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.TIMER_NOTIFICATION_ID
import com.nazmar.musicgym.TimerState
import java.text.SimpleDateFormat
import java.util.*

class Timer(private val runningNotification: NotificationCompat.Builder,
            private val pausedNotification: NotificationCompat.Builder,
            private val notificationManager: NotificationManager,
            private val mediaPlayer: MediaPlayer
) {

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
                    clearTimer()
                    showTimeUpNotification()
                    mediaPlayer.start()
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