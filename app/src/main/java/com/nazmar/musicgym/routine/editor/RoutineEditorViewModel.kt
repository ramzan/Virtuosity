package com.nazmar.musicgym.routine.editor

import androidx.lifecycle.*
import com.nazmar.musicgym.DEFAULT_TIMER_DURATION
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExerciseName
import kotlinx.coroutines.launch

class RoutineEditorViewModel(private val routineId: Long) : ViewModel() {

    val allExercises = Repository.getAllExercises().asLiveData()

    private var _state = MutableLiveData<RoutineEditorState>(RoutineEditorState.Loading)

    val state: LiveData<RoutineEditorState>
        get() = _state

    val exercises = Transformations.map(state) {
        it.exercises
    }

    init {
        if (routineId == 0L) {
            _state.value = RoutineEditorState.New(
                exercises = mutableListOf(),
                nameInputText = ""
            )
        } else {
            viewModelScope.launch {
                Repository.getRoutine(routineId).let {
                    _state.value = RoutineEditorState.Editing(
                        routine = it,
                        exercises = Repository.getRoutineExerciseNames(routineId).toMutableList(),
                        nameInputText = it.name
                    )
                }
            }
        }
    }

    fun updateDuration(exerciseIndex: Int, newDuration: Long) {
        (_state.value as RoutineEditorState.Editing).let {
            val newExercises = it.exercises.toMutableList()
            newExercises[exerciseIndex] = newExercises[exerciseIndex].copy(duration = newDuration)
            _state.value = it.copy(exercises = newExercises)
        }
    }

    fun deleteRoutine() {
        if (state.value is RoutineEditorState.Editing) {
            (state.value as RoutineEditorState.Editing).deleteRoutine()
            _state.value = RoutineEditorState.Deleted
        }
    }
}

sealed class RoutineEditorState {
    abstract val exercises: MutableList<RoutineExerciseName>
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
            RoutineExerciseName(
                exercise.id,
                exercise.name,
                DEFAULT_TIMER_DURATION
            )
        )
    }

    fun getItemDuration(index: Int): Long {
        return exercises[index].duration
    }

    object Loading : RoutineEditorState() {
        override val exercises = mutableListOf<RoutineExerciseName>()
        override var nameInputText: String = ""
    }

    object Deleted : RoutineEditorState() {
        override val exercises = mutableListOf<RoutineExerciseName>()
        override var nameInputText: String = ""
    }

    data class Editing(
        val routine: Routine,
        override val exercises: MutableList<RoutineExerciseName>,
        override var nameInputText: String,
    ) : RoutineEditorState() {
        fun deleteRoutine() = Repository.deleteRoutine(routine)

        fun saveRoutine() = Repository.updateRoutine(routine.id, nameInputText, exercises)
    }

    data class New(
        override val exercises: MutableList<RoutineExerciseName>,
        override var nameInputText: String = ""
    ) : RoutineEditorState() {
        fun saveRoutine() = Repository.createRoutine(nameInputText, exercises)

    }
}