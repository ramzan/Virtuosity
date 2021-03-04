package com.nazmar.musicgym.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nazmar.musicgym.session.SessionExercise
import com.nazmar.musicgym.session.SessionUseCase
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
                    if (this.currentIndex + 1 == this.sessionExercises.size) {
                        SessionState.SummaryScreen(this)
                    } else this.copy(currentIndex = this.currentIndex + 1)
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
                is SessionState.SummaryScreen -> {
                    _state.emit(oldState.backState)
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
        useCase.completeSession((_state.value as SessionState.SummaryScreen).summaryList)
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

    data class SummaryScreen(
        val backState: PracticeScreen
    ) : SessionState() {
        val summaryList
            get() = backState.sessionExercises.filter { e -> e.newBpm.isNotEmpty() && e.newBpm != "0" }
    }

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

        fun updateBpm(updatedBpm: String): MutableList<SessionExercise> {
            sessionExercises[currentIndex] =
                sessionExercises[currentIndex].copy(newBpm = updatedBpm)
            return sessionExercises
        }
    }
}