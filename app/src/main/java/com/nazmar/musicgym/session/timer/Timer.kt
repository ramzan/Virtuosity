package com.nazmar.musicgym.session.timer

import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.TIMER_NOTIFICATION_ID
import com.nazmar.musicgym.db.SessionExercise
import com.nazmar.musicgym.isOreoOrAbove
import com.nazmar.musicgym.toTimerString

enum class TimerState {
    STOPPED,
    RUNNING,
    PAUSED,
}

class Timer(
    private val runningNotification: NotificationCompat.Builder,
    private val pausedNotification: NotificationCompat.Builder,
    private val stoppedNotification: NotificationCompat.Builder,
    private val notificationManager: NotificationManager,
    private val timeRemainingPrefix: String,
    private val timeUpString: String,
    private val mediaPlayer: MediaPlayer,
    private val vibrator: Vibrator?
) {

    private var notification = stoppedNotification

    private fun updateTimerNotification() {
        notification.run {
            setContentTitle(currentExerciseName)
            setContentText(timeRemainingPrefix + " ${timeString.value}")
            notificationManager.notify(TIMER_NOTIFICATION_ID, build())
        }
    }

    private fun showTimeUpNotification() {
        notification.run {
            setContentTitle(currentExerciseName)
            setContentText(timeUpString)
            notificationManager.notify(TIMER_NOTIFICATION_ID, build())
        }
    }

    private fun showStoppedNotification() {
        notificationManager.notify(TIMER_NOTIFICATION_ID, stoppedNotification.build())
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

    private var currentExercise: SessionExercise? =
        SessionExercise(-1, -1, "", -1, -1)

    private val currentExerciseDuration
        get() = currentExercise?.duration ?: 0L

    private val currentExerciseName
        get() = currentExercise?.name ?: ""

    private fun createTimer() {
        with(timeLeft.value ?: currentExerciseDuration) {
            timer = object : CountDownTimer(this, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _timeLeft.value = millisUntilFinished
                    _timeString.value = millisUntilFinished.toTimerString()
                    updateTimerNotification()
                }

                override fun onFinish() {
                    clearTimer()
                    showTimeUpNotification()
                    vibrator?.vibrate()
                    mediaPlayer.start()
                }
            }
            _timeString.value = this.toTimerString()
            _timeLeft.value = this
        }
        updateTimerNotification()
    }

    fun setUpTimer(newExercise: SessionExercise?) {
        if (newExercise != currentExercise) {
            currentExercise = newExercise
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
        _timeString.value = 0L.toTimerString()
        notification = pausedNotification
        showStoppedNotification()
    }

    fun updateTimeLeft(newTime: Long) {
        timer?.cancel()
        _timeLeft.value = newTime
        if (newTime != 0L) createTimer()
    }

    @Suppress("DEPRECATION")
    private fun Vibrator.vibrate() {
        if (isOreoOrAbove()) {
            this.vibrate(VibrationEffect.createWaveform(longArrayOf(200, 250, 200, 250), -1))
        } else this.vibrate(longArrayOf(200, 250, 200, 250), -10)

    }
}