package ca.ramzan.virtuosity.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.session.SessionExercise
import ca.ramzan.virtuosity.session.SessionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel @AssistedInject constructor(
    @Assisted routineId: Long,
    private val useCase: SessionUseCase
) :
    ViewModel() {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)

    val state: StateFlow<SessionState> get() = _state

    init {
        viewModelScope.launch {
            val name = useCase.getRoutineName(routineId)
            val exercises = useCase.getSession(routineId)
            _state.emit(
                if (exercises.isEmpty()) {
                    SessionState.EmptyRoutine
                } else {
                    SessionState.PracticeScreen(
                        name,
                        exercises,
                        0
                    )
                }
            )
        }
    }

    fun nextExercise() {
        viewModelScope.launch {
            (_state.value as? SessionState.PracticeScreen)?.run {
                _state.emit(
                    this.copy(currentIndex = this.currentIndex + 1)
                )
            }
        }
    }

    fun previousExercise() {
        viewModelScope.launch {
            when (val oldState = _state.value) {
                is SessionState.PracticeScreen -> {
                    _state.emit(oldState.copy(currentIndex = oldState.currentIndex - 1))
                }
            }
        }
    }

    fun updateBpm(bpm: String) {
        viewModelScope.launch {
            (_state.value as? SessionState.PracticeScreen)?.let { oldState ->
                val newState = oldState.copy(sessionExercises = oldState.updateBpm(bpm))
                useCase.updateSessionState(newState.currentExercise)
                _state.emit(newState)
            }
        }
    }

    fun completeSession() {
        useCase.completeSession((_state.value as SessionState.PracticeScreen).sessionExercises.filter { e -> e.newBpm.isNotEmpty() && e.newBpm != "0" })
    }

    fun cancelSession() = useCase.clearSavedSession()

    // region Factory ------------------------------------------------------------------------------

    @AssistedFactory
    interface Factory {
        fun create(routine: Long): SessionViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            routineId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(routineId) as T
            }
        }
    }
    // endregion Factory ---------------------------------------------------------------------------
}

sealed class SessionState {
    object Loading : SessionState()

    object EmptyRoutine : SessionState()

    data class PracticeScreen(
        val sessionName: String,
        val sessionExercises: MutableList<SessionExercise>,
        val currentIndex: Int
    ) : SessionState() {

        val currentExercise: SessionExercise
            get() = sessionExercises[currentIndex]

        val currentExerciseName: String
            get() = currentExercise.name

        val currentExerciseBpmRecord: String
            get() = currentExercise.bpmRecord.toString()

        val newExerciseBpm: String
            get() = currentExercise.newBpm

        val previousButtonEnabled: Boolean
            get() = currentIndex > 0

        val nextButtonEnabled: Boolean
            get() = currentIndex < sessionExercises.size - 1

        fun updateBpm(updatedBpm: String): MutableList<SessionExercise> {
            val newList = sessionExercises.toMutableList()
            newList[currentIndex] = newList[currentIndex].copy(newBpm = updatedBpm)
            return newList
        }
    }
}