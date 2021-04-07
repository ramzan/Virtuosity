package ca.ramzan.virtuosity.screens.routineeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.common.DEFAULT_TIMER_DURATION
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineEditorUseCase
import ca.ramzan.virtuosity.routine.RoutineExercise
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

    var indexToUpdate: Int = -1

    init {
        if (routineId == 0L) {
            _state.value = RoutineEditorState.New(
                exercises = mutableListOf(),
                nameInputText = ""
            )
        } else {
            viewModelScope.launch {
                _state.value = useCase.getRoutine(routineId)?.let {
                    RoutineEditorState.Editing(
                        routine = it,
                        exercises = useCase.getRoutineExerciseNames(routineId).toMutableList(),
                        nameInputText = it.name
                    )
                } ?: RoutineEditorState.Deleted
            }
        }
    }

    fun updateDuration(newDuration: Long) {
        if (indexToUpdate == -1) return
        val oldState = _state.value
        val newExercises = oldState.exercises.toMutableList().apply {
            set(indexToUpdate, get(indexToUpdate).copy(duration = newDuration))
        }

        if (oldState is RoutineEditorState.Editing) {
            _state.value = oldState.copy(exercises = newExercises)
        } else if (oldState is RoutineEditorState.New) {
            _state.value = oldState.copy(exercises = newExercises)
        }
        indexToUpdate = -1
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

    fun deleteItem(index: Int): RoutineExercise {
        return _state.value.exercises.removeAt(index)
    }

    fun undoDelete(exercise: RoutineExercise, position: Int) {
        _state.value.exercises.add(position, exercise)
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

// region Factory ------------------------------------------------------------------------------

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

// endregion Factory ---------------------------------------------------------------------------
}

sealed class RoutineEditorState {
    abstract val exercises: MutableList<RoutineExercise>
    abstract var nameInputText: String

    object Loading : RoutineEditorState() {
        override val exercises = mutableListOf<RoutineExercise>()
        override var nameInputText: String = ""
    }

    object Deleted : RoutineEditorState() {
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