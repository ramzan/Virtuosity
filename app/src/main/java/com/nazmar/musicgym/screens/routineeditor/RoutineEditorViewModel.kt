package com.nazmar.musicgym.screens.routineeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nazmar.musicgym.common.DEFAULT_TIMER_DURATION
import com.nazmar.musicgym.exercises.Exercise
import com.nazmar.musicgym.routine.Routine
import com.nazmar.musicgym.routine.RoutineEditorUseCase
import com.nazmar.musicgym.routine.RoutineExercise
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutineEditorViewModel @AssistedInject constructor(
    @Assisted private val routineId: Long,
    private val useCase: RoutineEditorUseCase
) : ViewModel() {

    val allExercises = useCase.getAllExercises()

    private var _state = MutableStateFlow<RoutineEditorState>(RoutineEditorState.Loading)
    val state: StateFlow<RoutineEditorState>
        get() = _state

    var indexPendingDurationChange: Int? = null

    init {
        if (routineId == 0L) {
            _state.value = RoutineEditorState.New(
                exercises = mutableListOf(),
                nameInputText = ""
            )
        } else {
            viewModelScope.launch {
                useCase.getRoutine(routineId).let {
                    _state.value = RoutineEditorState.Editing(
                        routine = it,
                        exercises = useCase.getRoutineExerciseNames(routineId).toMutableList(),
                        nameInputText = it.name
                    )
                }
            }
        }
    }

    fun updateDuration(newDuration: Long) {
        (_state.value as RoutineEditorState.Editing).let {
            indexPendingDurationChange?.let { index ->
                val newExercises = it.exercises.toMutableList()
                newExercises[index] = newExercises[index].copy(duration = newDuration)
                _state.value = it.copy(exercises = newExercises)
                indexPendingDurationChange = null
            }
        }
    }

    fun deleteRoutine() {
        (state.value as? RoutineEditorState.Editing)?.let {
            useCase.deleteRoutine(it.routine)
        }
    }

    fun saveRoutine() {
        when (val state = _state.value) {
            is RoutineEditorState.Editing -> {
                useCase.updateRoutine(state.routine.id, state.nameInputText, state.exercises)
            }
            is RoutineEditorState.New -> {
                useCase.createRoutine(state.nameInputText, state.exercises)
            }
            else -> return
        }
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        _state.value.exercises.run {
            this.add(toPos, this.removeAt(fromPos))
        }
        return true
    }

    fun deleteItem(index: Int) {
        _state.value.exercises.removeAt(index)
    }

    fun addExercise(exercise: Exercise) {
        _state.value.exercises.add(
            RoutineExercise(
                exercise.id,
                exercise.name,
                DEFAULT_TIMER_DURATION
            )
        )
    }

    // region Factory -----------------------------------------------------------------------------------

    @AssistedFactory
    interface Factory {
        fun create(routineId: Long): RoutineEditorViewModel
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

    // endregion Factory -----------------------------------------------------------------------------------
}

sealed class RoutineEditorState {
    abstract val exercises: MutableList<RoutineExercise>
    abstract var nameInputText: String

    object Loading : RoutineEditorState() {
        override val exercises = mutableListOf<RoutineExercise>()
        override var nameInputText: String = ""
    }

    data class Editing(
        val routine: Routine,
        override val exercises: MutableList<RoutineExercise>,
        override var nameInputText: String,
    ) : RoutineEditorState()

    data class New(
        override val exercises: MutableList<RoutineExercise>,
        override var nameInputText: String = ""
    ) : RoutineEditorState()
}