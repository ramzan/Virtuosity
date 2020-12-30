package com.nazmar.musicgym.practice.session

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class TimerState {
    STOPPED, // Timer has not been created
    RUNNING, // Timer is counting down
    PAUSED, // Pause button pressed, time still remaining
    FINISHED, // Timer has reached 0, sound the alarm
    COMPLETED // Timer at 0 and alarm has been rung
}

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercises = dao.getSessionExercises(routineId)

    private var newBpms = mutableListOf<String>()

    private var _exercisesLoaded = newBpms.size != 0

    val exercisesLoaded: Boolean
        get() = _exercisesLoaded

    private var _currentIndex = MutableLiveData(-1)

    val currentIndex: LiveData<Int>
        get() = _currentIndex

    fun createBpmList() {
        exercises.value?.forEach { _ ->
            newBpms.add("")
        }
        _exercisesLoaded = true
    }

    fun nextExercise() {
        clearTimer()
        _currentIndex.value = _currentIndex.value!! + 1
    }

    fun previousExercise() {
        clearTimer()
        _currentIndex.value = _currentIndex.value!! - 1
    }

    fun getCurrentExerciseName(): String {
        return when (exercisesLoaded && currentIndex.value!! > -1) {
            true -> exercises.value!![currentIndex.value!!].name
            else -> ""
        }
    }

    fun getCurrentExerciseBpmRecord(): String {
        return when (exercisesLoaded && currentIndex.value!! > -1) {
            true -> exercises.value!![currentIndex.value!!].bpm.toString()
            else -> ""
        }
    }

    fun getNewExerciseBpm(): String? {
        return when (exercisesLoaded && currentIndex.value!! > -1) {
            true -> newBpms[currentIndex.value!!]
            else -> null
        }
    }

    fun nextButtonEnabled(): Boolean {
        return exercisesLoaded && currentIndex.value!! < exercises.value!!.size - 1
    }

    fun previousButtonEnabled(): Boolean {
        return exercisesLoaded && currentIndex.value!! > 0
    }

    fun updateBpm(bpm: String) {
        if (exercisesLoaded) {
            currentIndex.value?.let { newBpms[it] = bpm }
        }
    }

    fun saveSession() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until newBpms.size) {
                val bpm = newBpms[i]
                if (bpm.isNotEmpty()) {
                    dao.insert(HistoryItem(exercises.value!![i].exerciseId, bpm.toInt()))
                }
            }
        }
    }

    private fun currentExerciseDuration(): Long {
        return exercises.value!![currentIndex.value!!].duration * 1000
    }

    /************************** Timer **********************************/

    private var timer: CountDownTimer? = null

    private var _timeLeft = MutableLiveData<Long>(null)

    val timeLeft: LiveData<Long>
        get() = _timeLeft

    val timeString = Transformations.map(timeLeft) { time ->
        (time ?: 0L).let { "${it / 60000}:" + "${(it / 1000) % 60}".padStart(2, '0') }
    }

    private var _timerStatus = MutableLiveData(TimerState.STOPPED)

    val timerStatus: LiveData<TimerState>
        get() = _timerStatus

    private fun createTimer() {
        timer = object : CountDownTimer(timeLeft.value ?: currentExerciseDuration(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                _timerStatus.value = TimerState.FINISHED
                _timeLeft.value = null
            }
        }
        _timeLeft.value = timeLeft.value ?: currentExerciseDuration()
    }

    fun setUpTimer() {
        if (timerStatus.value == TimerState.STOPPED && currentExerciseDuration() != 0L) {
            createTimer()
            startTimer()
        }
    }

    fun startTimer() {
        timer?.let {
            it.start()
            _timerStatus.value = TimerState.RUNNING
        }
    }

    fun pauseTimer() {
        if (timerStatus.value != TimerState.FINISHED) {
            timer?.cancel()
            createTimer()
            _timerStatus.value = TimerState.PAUSED
        }
    }

    fun onAlarmRung() {
        timer = null
        _timeLeft.value = null
        _timerStatus.value = TimerState.COMPLETED
    }

    fun restartTimer() {
        timer?.cancel()
        _timeLeft.value = null
        if (currentExerciseDuration() != 0L) {
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