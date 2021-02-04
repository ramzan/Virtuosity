package com.nazmar.musicgym.session

import androidx.lifecycle.*
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.SessionExercise
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long) : ViewModel() {

    private var _sessionName = MutableLiveData("")

    val sessionName: LiveData<String> = _sessionName

    init {
        viewModelScope.launch {
            _sessionName.value = Repository.getRoutine(routineId).name
        }
    }

    private var _exercises = MutableLiveData<MutableList<SessionExercise>>()

    val exercises: LiveData<MutableList<SessionExercise>>
        get() = _exercises

    val summaryList = Transformations.map(exercises) {
        it.filter { e -> e.newBpm.isNotEmpty() && e.newBpm != "0" }
    }

    init {
        viewModelScope.launch {
            _exercises.value = Repository.getSession(routineId)
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
            _exercises.value?.let { exerciseList ->
                Repository.updateSessionState(exerciseList[it])
            }

        }
    }

    private fun MutableLiveData<MutableList<SessionExercise>>.updateBpm(
        index: Int,
        updatedBpm: String
    ) {
        val value = this.value?.toMutableList() ?: mutableListOf()
        value[index] = value[index].copy(newBpm = updatedBpm)
        this.value = value

    }

    fun completeSession() = summaryList.value?.let { Repository.completeSession(it) }

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