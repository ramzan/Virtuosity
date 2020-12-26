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
        resetTimer()
        _currentIndex.value = _currentIndex.value!! + 1
    }

    fun previousExercise() {
        resetTimer()
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

    private var timer: CountDownTimer? = null

    private var _timeUp = MutableLiveData(false)

    val timeUp: LiveData<Boolean>
        get() = _timeUp

    private var _timeString = MutableLiveData(emptyTimeString)

    val timeString: LiveData<String>
        get() = _timeString

    private var savedTime: Long? = null

    fun startTimer() {
        if (timer == null && savedTime != 0L && currentExerciseDuration() != 0L) {
            timer = object : CountDownTimer(savedTime ?: currentExerciseDuration(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    savedTime = millisUntilFinished
                    _timeString.value = "${(millisUntilFinished / 60000) % 60}:" + "${(millisUntilFinished / 1000) % 60}".padStart(2, '0')
                }

                override fun onFinish() {
                    _timeString.value = emptyTimeString
                    _timeUp.value = true
                }
            }
            (timer as CountDownTimer).start()
        }
    }

    fun onTimeUp() {
        savedTime = 0L
        _timeUp.value = false
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun resetTimer() {
        stopTimer()
        savedTime = null
        _timeUp.value = false
        _timeString.value = emptyTimeString
    }

}