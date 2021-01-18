package com.nazmar.musicgym.practice.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val session = dao.getRoutine(routineId)

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
        if (newBpms.size > 0) _exercisesLoaded = true
    }

    fun nextExercise() {
        if (exercisesLoaded) {
            _currentIndex.value = _currentIndex.value!! + 1
        }
    }

    fun previousExercise() {
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
        return exercisesLoaded && currentIndex.value!! < exercises.value!!.size
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

    fun currentExerciseDuration(): Long {
        return (currentIndex.value?.let { exercises.value?.get(it)?.duration } ?: 0L)
    }

    // Timer editor

    private var _editorTime = MutableLiveData<Long?>(null)

    val editorTime: LiveData<Long?>
        get() = _editorTime

    fun updateEditorTime(time: Long) {
        _editorTime.value = time
    }

    fun clearEditorTime() {
        _editorTime.value = null
    }
}