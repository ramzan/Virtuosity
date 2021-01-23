package com.nazmar.musicgym.practice.session

import androidx.lifecycle.*
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.SessionExercise
import com.nazmar.musicgym.updateBpm
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long) : ViewModel() {

    val session = Repository.getRoutine(routineId)

    private var _exercises = MutableLiveData<MutableList<SessionExercise>>()

    val exercises: LiveData<MutableList<SessionExercise>>
        get() = _exercises

    val summaryList = Transformations.map(exercises) {
        it.filter { e -> e.newBpm.isNotEmpty() }
    }

    init {
        viewModelScope.launch {
            _exercises.value = Repository.getSession(routineId, false)
        }
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

    fun completeSession() = exercises.value?.let { Repository.completeSession(it) }

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