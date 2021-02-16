package com.nazmar.musicgym.screens.routineeditor

import androidx.lifecycle.*
import com.nazmar.musicgym.common.DEFAULT_TIMER_DURATION
import com.nazmar.musicgym.exercises.Exercise
import com.nazmar.musicgym.routine.Routine
import com.nazmar.musicgym.routine.RoutineEditorUseCase
import com.nazmar.musicgym.routine.RoutineExercise
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class RoutineEditorViewModel @AssistedInject constructor(
    @Assisted private val routineId: Long,
    private val useCase: RoutineEditorUseCase
) : ViewModel() {

    val allExercises = useCase.getAllExercises().asLiveData()

    private var _state = MutableLiveData<RoutineEditorState>(RoutineEditorState.Loading)

    val state: LiveData<RoutineEditorState>
        get() = _state

    val exercises = Transformations.map(state) {
        it.exercises
    }

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
            _state.value = RoutineEditorState.Deleted
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

    // Factory -----------------------------------------------------------------------------------

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
}

sealed class RoutineEditorState {
    abstract val exercises: MutableList<RoutineExercise>
    abstract var nameInputText: String

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        exercises.add(toPos, exercises.removeAt(fromPos))
        return true
    }

    fun deleteItem(index: Int) {
        exercises.removeAt(index)
    }

    fun addExercise(exercise: Exercise) {
        exercises.add(
            RoutineExercise(
                exercise.id,
                exercise.name,
                DEFAULT_TIMER_DURATION
            )
        )
    }

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