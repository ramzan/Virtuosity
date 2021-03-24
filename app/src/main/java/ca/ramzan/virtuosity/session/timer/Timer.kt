package ca.ramzan.virtuosity.session.timer

import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import ca.ramzan.virtuosity.common.TIMER_NOTIFICATION_ID
import ca.ramzan.virtuosity.common.isOreoOrAbove
import ca.ramzan.virtuosity.common.millisToTimerString
import ca.ramzan.virtuosity.session.SessionExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
    private val vibrator: Vibrator?,
    private val timerScope: CoroutineScope
) {

    var notification = stoppedNotification

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

    private var _timeLeft = MutableStateFlow<Long?>(null)

    val timeLeft: StateFlow<Long?>
        get() = _timeLeft

    val timeLeftPercent = timeLeft.map { time ->
        if (currentExerciseDuration == 0L || time == null) 0 else (time * 100 / startingTime).toInt()
    }

    private var startingTime = 0L

    private val _timeString = MutableStateFlow("")

    val timeString: StateFlow<String>
        get() = _timeString

    private var _status = MutableStateFlow(TimerState.STOPPED)

    val status: StateFlow<TimerState>
        get() = _status

    private var currentExercise: SessionExercise? = null

    private val currentExerciseDuration
        get() = currentExercise?.duration ?: 0L

    private val currentExerciseName
        get() = currentExercise?.name ?: ""

    private fun createTimer() {
        val initialTime = timeLeft.value ?: currentExerciseDuration
        startingTime = initialTime
        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerScope.launch(Dispatchers.Main) {
                    _timeLeft.emit(millisUntilFinished)
                    _timeString.emit(millisToTimerString(millisUntilFinished))
                }

                updateTimerNotification()
            }

            override fun onFinish() {
                clearTimer()
                showTimeUpNotification()
                vibrator?.vibrate()
                mediaPlayer.start()
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            _timeLeft.emit(initialTime)
            _timeString.emit(millisToTimerString(initialTime))
        }

        updateTimerNotification()
    }

    fun setUpTimer(newExercise: SessionExercise) {
        if (newExercise.order == currentExercise?.order) return
        currentExercise = newExercise
        clearTimer()
        if (currentExerciseDuration != 0L) {
            createTimer()
            startTimer()
        }
    }

    fun startTimer() {
        timer?.let {
            notification = runningNotification
            it.start()
            _status.value = TimerState.RUNNING
        }
    }

    fun pauseTimer() {
        notification = pausedNotification
        timer?.cancel()
        createTimer()
        _status.value = TimerState.PAUSED
    }

    fun restartTimer() {
        timer?.cancel()
        _timeLeft.value = null
        if (currentExerciseDuration != 0L) {
            createTimer()
            if (status.value == TimerState.RUNNING) {
                startTimer()
            } else {
                _status.value = TimerState.PAUSED
            }
        } else {
            _status.value = TimerState.STOPPED
        }
    }

    fun clearTimer() {
        timer?.cancel()
        timer = null
        _status.value = TimerState.STOPPED
        _timeLeft.value = null
        _timeString.value = millisToTimerString(0L)
        notification = pausedNotification
        showStoppedNotification()
    }

    fun clearExercise() {
        clearTimer()
        currentExercise = null
        notification = stoppedNotification
    }

    fun updateTimeLeft(newTime: Long) {
        timer?.cancel()
        startingTime = newTime
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