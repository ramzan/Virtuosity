package ca.ramzan.virtuosity.screens.routine_editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.common.DEFAULT_TIMER_DURATION
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineEditorExercise
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

    private var listIdCount = 0L

    private var _state = MutableStateFlow<RoutineEditorState>(RoutineEditorState.Loading)
    val state: StateFlow<RoutineEditorState>
        get() = _state

    var indexToUpdate: Int = -1

    var firstRun = true

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
                        exercises = useCase.getRoutineExerciseNames(routineId)
                            .toEditorExercises()
                            .toMutableList(),
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
                useCase.updateRoutine(
                    state.routine.id,
                    state.nameInputText,
                    state.exercises.toRoutineExercises()
                )
            }
            is RoutineEditorState.New -> {
                useCase.createRoutine(state.nameInputText, state.exercises.toRoutineExercises())
            }
            else -> return
        }
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        val oldState = _state.value
        val temp = oldState.exercises.toMutableList()
        temp.add(toPos, temp.removeAt(fromPos))
        updateExercises(oldState, temp)
        return true
    }

    fun deleteItem(index: Int): RoutineEditorExercise {
        val oldState = _state.value
        val temp = oldState.exercises.toMutableList()
        val deleted = temp.removeAt(index)
        updateExercises(oldState, temp)
        return deleted
    }

    fun undoDelete(exercise: RoutineEditorExercise, position: Int) {
        val oldState = _state.value
        val temp = oldState.exercises.toMutableList()
        temp.add(position, exercise)
        updateExercises(oldState, temp)
    }

    fun addExercises(exercises: List<Exercise>) {
        val oldState = _state.value
        val temp = oldState.exercises.toMutableList()
        temp.addAll(
            exercises.map { exercise ->
                RoutineEditorExercise(
                    listIdCount++,
                    exercise.id,
                    exercise.name,
                    DEFAULT_TIMER_DURATION
                )
            }
        )
        updateExercises(oldState, temp)
    }

    private fun updateExercises(
        oldState: RoutineEditorState,
        newExercises: List<RoutineEditorExercise>
    ) {
        if (oldState is RoutineEditorState.New) {
            _state.value = oldState.copy(exercises = newExercises)
        } else if (oldState is RoutineEditorState.Editing) {
            _state.value = oldState.copy(exercises = newExercises)
        }
    }

    val exercises get() = state.value.exercises

    private fun List<RoutineExercise>.toEditorExercises(): List<RoutineEditorExercise> {
        return map {
            RoutineEditorExercise(listIdCount++, it.exerciseId, it.name, it.duration)
        }
    }

    private fun List<RoutineEditorExercise>.toRoutineExercises(): List<RoutineExercise> {
        return map {
            RoutineExercise(it.exerciseId, it.name, it.duration)
        }
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
    abstract val exercises: List<RoutineEditorExercise>
    abstract var nameInputText: String

    object Loading : RoutineEditorState() {
        override val exercises = listOf<RoutineEditorExercise>()
        override var nameInputText: String = ""
    }

    object Deleted : RoutineEditorState() {
        override val exercises = listOf<RoutineEditorExercise>()
        override var nameInputText: String = ""
    }

    data class Editing(
        val routine: Routine,
        override val exercises: List<RoutineEditorExercise>,
        override var nameInputText: String,
    ) : RoutineEditorState()

    data class New(
        override val exercises: List<RoutineEditorExercise>,
        override var nameInputText: String = ""
    ) : RoutineEditorState()
}