package com.nazmar.musicgym.practice.session

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val emptyTimeString = "0:00"

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

    private var _timeString = MutableLiveData(emptyTimeString)

    val timeString: LiveData<String>
        get() = _timeString

    private var timeLeft: Long? = null

    private var _timerStatus = MutableLiveData(TimerState.STOPPED)

    val timerStatus: LiveData<TimerState>
        get() = _timerStatus

    private fun createTimer() {
        timer = object : CountDownTimer(timeLeft ?: currentExerciseDuration(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                _timeString.value = timeToText(millisUntilFinished)
            }

            override fun onFinish() {
                _timeString.value = emptyTimeString
                _timerStatus.value = TimerState.FINISHED
                timeLeft = null
            }
        }
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
        timeLeft = null
        _timerStatus.value = TimerState.COMPLETED
    }

    fun restartTimer() {
        timer?.cancel()
        createTimer()
        timeLeft = currentExerciseDuration()
        _timeString.value = timeToText(currentExerciseDuration())
        if (timerStatus.value == TimerState.RUNNING) {
            startTimer()
        } else {
            _timerStatus.value = TimerState.PAUSED
        }
    }

    fun clearTimer() {
        timer?.cancel()
        timer = null
        _timerStatus.value = TimerState.STOPPED
        timeLeft = null
    }

    fun timeToText(millisUntilFinished: Long): String {
        return "${(millisUntilFinished / 60000) % 60}:" + "${(millisUntilFinished / 1000) % 60}".padStart(2, '0')
    }
}

