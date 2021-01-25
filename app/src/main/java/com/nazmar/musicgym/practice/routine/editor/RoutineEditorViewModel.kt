package com.nazmar.musicgym.practice.routine.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.DEFAULT_TIMER_DURATION
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.RoutineExerciseName
import java.time.Duration

class RoutineEditorViewModel(private val routineId: Long) : ViewModel() {

    val newRoutine = routineId == 0L

    val routine = Repository.getRoutine(routineId)

    private var _routineDeleted = false

    val routineDeleted: Boolean
        get() = _routineDeleted

    val exercises = Repository.getAllExercises()

    val oldExercises = Repository.getRoutineExerciseNames(routineId)

    private var _currentExercises = mutableListOf<RoutineExerciseName>()

    val currentExercises: MutableList<RoutineExerciseName>
        get() = _currentExercises

    private var currentExercisesLoaded = false

    private var _updatedIndex = MutableLiveData<Int>()

    val updatedIndex: LiveData<Int>
        get() = _updatedIndex

    var nameInputText: String = ""

    fun loadOldRoutine() {
        if (!currentExercisesLoaded) {
            oldExercises.value?.let {
                _currentExercises = it.toMutableList()
                currentExercisesLoaded = true
            }
        }
    }

    fun deleteRoutine() {
        routine.value?.let {
            Repository.deleteRoutine(it)
            _routineDeleted = true
        }
    }

    fun updateRoutine() {
        when (newRoutine) {
            true -> Repository.createRoutine(nameInputText, currentExercises)
            false -> Repository.updateRoutine(routineId, nameInputText, currentExercises)
        }
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        _currentExercises.add(toPos, _currentExercises.removeAt(fromPos))
        return true
    }

    fun deleteItem(index: Int) {
        _currentExercises.removeAt(index)
    }

    fun updateDuration(exerciseIndex: Int, newDuration: Long) {
        with(currentExercises[exerciseIndex]) {
            this.duration = Duration.ofMillis(newDuration)
            _updatedIndex.value = exerciseIndex
        }
    }

    fun addExercise(exercise: Exercise) {
        currentExercises.add(
            RoutineExerciseName(
                exercise.id,
                exercise.name,
                Duration.ofMillis(DEFAULT_TIMER_DURATION)
            )
        )
    }

    fun getItemDuration(index: Int): Duration {
        return currentExercises[index].duration
    }
}