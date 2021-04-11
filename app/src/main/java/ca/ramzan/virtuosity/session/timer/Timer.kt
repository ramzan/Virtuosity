package ca.ramzan.virtuosity.session.timer

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import ca.ramzan.virtuosity.common.isOreoOrAbove
import ca.ramzan.virtuosity.common.millisToTimerString
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
    private val notificationManager: TimerNotificationManager,
    private val mediaPlayer: MediaPlayer,
    private val vibrator: Vibrator?,
    private val timerScope: CoroutineScope
) {

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

    init {
        timerScope.launch(Dispatchers.Main) {
            timeString.collect {
                notificationManager.updateTimerNotification(
                    currentExerciseName,
                    timeString.value,
                    status.value
                )
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
                notificationManager.showTimeUpNotification(currentExerciseName)
                vibrator?.vibrate()
                mediaPlayer.start()
            }
        }
        emitTime(initialTime)
    }

    fun setUpTimer(newOrder: Int, newName: String, newDuration: Long) {
        // Don't restart timer on config change
        if (newOrder == currentExerciseOrder) return

        currentExerciseOrder = newOrder
        currentExerciseName = newName
        startingDuration = newDuration
        stopTimer()
        if (startingDuration != 0L) {
            createTimer()
            startTimer()
        }
    }

    fun startTimer() {
        timer?.run {
            start()
            setStatus(TimerState.RUNNING)
        }
    }

    fun pauseTimer() {
        timer?.cancel()
        setStatus(TimerState.PAUSED)
        createTimer()
    }

    fun restartTimer() {
        timer?.cancel()
        _timeLeft.value = null
        if (startingDuration != 0L) {
            createTimer()
            if (status.value == TimerState.RUNNING) startTimer() else setStatus(TimerState.PAUSED)
        } else setStatus(TimerState.STOPPED)
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
        _timeLeft.value = null
        timerScope.launch(Dispatchers.Main) { _timeString.emit(millisToTimerString(0L)) }
        setStatus(TimerState.STOPPED)
    }

    // Called when timer duration is edited
    fun updateTimeLeft(newTime: Long) {
        timer?.cancel()
        timer = null
        startingDuration = newTime
        _timeLeft.value = null
        if (newTime != 0L) {
            setStatus(TimerState.PAUSED)
            createTimer()
        } else {
            emitTime(0)
            setStatus(TimerState.STOPPED)
        }
    }

    private fun emitTime(time: Long) {
        timerScope.launch(Dispatchers.Main) {
            _timeLeft.emit(time)
            _timeString.emit(millisToTimerString(time))
        }
    }

    private fun setStatus(newStatus: TimerState) {
        _status.value = newStatus
        notificationManager.updateTimerNotification(
            currentExerciseName,
            timeString.value,
            newStatus
        )
    }

    @Suppress("DEPRECATION")
    private fun Vibrator.vibrate() {
        if (isOreoOrAbove()) {
            this.vibrate(VibrationEffect.createWaveform(longArrayOf(200, 250, 200, 250), -1))
        } else this.vibrate(longArrayOf(200, 250, 200, 250), -10)
    }
}