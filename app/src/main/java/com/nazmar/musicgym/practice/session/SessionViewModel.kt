package com.nazmar.musicgym.practice.session

import android.app.Application
import androidx.lifecycle.*
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import com.nazmar.musicgym.db.SessionExercise
import com.nazmar.musicgym.updateBpm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val session = dao.getRoutine(routineId)

    private var _exercises = MutableLiveData<MutableList<SessionExercise>>()

    init {
        viewModelScope.launch {
            _exercises.value = dao.getSessionExercises(routineId)
        }
    }

    val exercises: LiveData<MutableList<SessionExercise>>
        get() = _exercises

    val summaryList = Transformations.map(exercises) {
        it.filter { e -> e.newBpm.isNotEmpty() }
    }

    private var _currentIndex = MutableLiveData(-1)

    val currentIndex: LiveData<Int>
        get() = _currentIndex

    fun nextExercise() {
        _currentIndex.value = _currentIndex.value!! + 1
    }

    fun previousExercise() {
        _currentIndex.value = _currentIndex.value!! - 1
    }

    val currentExercise = Transformations.map(currentIndex) {
        exercises.value?.let { exercises ->
            when (it) {
                exercises.size, -1 -> null
                else -> exercises[it]
            }
        }
    }

    val currentExerciseName: String
        get() = currentExercise.value?.name ?: ""

    val currentExerciseBpmRecord: String
        get() = (currentExercise.value?.bpmRecord ?: 0).toString()

    val newExerciseBpm: String
        get() = currentExercise.value?.newBpm ?: ""

    val nextButtonEnabled: Boolean
        get() = currentIndex.value!! > -1 && currentIndex.value!! < exercises.value!!.size

    val previousButtonEnabled: Boolean
        get() = currentIndex.value!! > 0

    fun updateBpm(bpm: String) {
        currentIndex.value?.let {
            _exercises.updateBpm(it, bpm)
        }
    }

    fun saveSession() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in exercises.value!!.indices) {
                exercises.value?.get(i)?.let { exercise ->
                    if (exercise.newBpm.isNotEmpty()) {
                        dao.insert(HistoryItem(exercise.exerciseId, exercise.newBpm.toInt()))
                    }
                }
            }
        }
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