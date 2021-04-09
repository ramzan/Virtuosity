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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class TimerState {
    STOPPED, // Before any exercises are loaded OR when the current exercise has 0 duration
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

    // region notifications ------------------------------------------------------------------------

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

    // endregion notifications ---------------------------------------------------------------------

    // region state --------------------------------------------------------------------------------

    private var timer: CountDownTimer? = null

    private var startingDuration = 0L

    // Order/index of exercise in the routine
    private var currentExerciseOrder: Int? = null

    private var currentExerciseName = ""

    private var _timeLeft = MutableStateFlow<Long?>(null)
    val timeLeft: StateFlow<Long?> get() = _timeLeft

    private val _timeString = MutableStateFlow("")
    val timeString: StateFlow<String> get() = _timeString

    private var _status = MutableStateFlow(TimerState.STOPPED)
    val status: StateFlow<TimerState> get() = _status

    // endregion state -----------------------------------------------------------------------------

    init {
        timerScope.launch(Dispatchers.Main) {
            timeString.collect {
                if (status.value == TimerState.STOPPED) showStoppedNotification()
                else updateTimerNotification()
            }
        }
    }

    private fun createTimer() {
        val initialTime = timeLeft.value ?: startingDuration
        timer = object : CountDownTimer(initialTime, 16) {
            override fun onTick(millisUntilFinished: Long) {
                emitTime(millisUntilFinished)
            }

            override fun onFinish() {
                stopTimer()
                notification = pausedNotification
                showTimeUpNotification()
                vibrator?.vibrate()
                mediaPlayer.start()
            }
        }
        emitTime(initialTime)
    }

    fun setUpTimer(newExercise: SessionExercise) {
        // Don't restart timer on config change
        if (newExercise.order == currentExerciseOrder) return

        currentExerciseOrder = newExercise.order
        currentExerciseName = newExercise.name
        startingDuration = newExercise.duration
        stopTimer()
        if (startingDuration != 0L) {
            createTimer()
            startTimer()
        }
    }

    fun startTimer() {
        timer?.let {
            notification = runningNotification
            it.start()
            updateTimerNotification()
            _status.value = TimerState.RUNNING
        }
    }

    fun pauseTimer() {
        notification = pausedNotification
        timer?.cancel()
        updateTimerNotification()
        _status.value = TimerState.PAUSED
        createTimer()
    }

    fun restartTimer() {
        timer?.cancel()
        _timeLeft.value = null
        if (startingDuration != 0L) {
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

    fun stopTimer() {
        timer?.cancel()
        timer = null
        _status.value = TimerState.STOPPED
        _timeLeft.value = null
        _timeString.value = millisToTimerString(0L)
        showStoppedNotification()
    }

    // Called when timer duration is edited
    fun updateTimeLeft(newTime: Long) {
        timer?.cancel()
        timer = null
        startingDuration = newTime
        _timeLeft.value = null
        if (newTime != 0L) {
            _status.value = TimerState.PAUSED
            createTimer()
        } else {
            _status.value = TimerState.STOPPED
            emitTime(0)
        }
    }

    private fun emitTime(time: Long) {
        timerScope.launch(Dispatchers.Main) {
            _timeLeft.emit(time)
            _timeString.emit(millisToTimerString(time))
        }
    }

    @Suppress("DEPRECATION")
    private fun Vibrator.vibrate() {
        if (isOreoOrAbove()) {
            this.vibrate(VibrationEffect.createWaveform(longArrayOf(200, 250, 200, 250), -1))
        } else this.vibrate(longArrayOf(200, 250, 200, 250), -10)
    }
}