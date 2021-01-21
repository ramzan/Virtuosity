package com.nazmar.musicgym.practice.routine.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.DEFAULT_TIMER_DURATION
import com.nazmar.musicgym.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineEditorViewModel(private val routineId: Long, application: Application) :
    AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val newRoutine = routineId == 0L

    val routine = dao.getRoutine(routineId)

    private var _routineDeleted = false

    val routineDeleted: Boolean
        get() = _routineDeleted

    val exercises = dao.getAllExercises()

    val oldExercises = dao.getRoutineExerciseNames(routineId)

    private var _currentExercises = mutableListOf<RoutineExerciseName>()

    val currentExercises: MutableList<RoutineExerciseName>
        get() = _currentExercises

    private var currentExercisesLoaded = false

    private var _updatedIndex = MutableLiveData<Int>()

    val updatedIndex: LiveData<Int>
        get() = _updatedIndex

    var nameInputText: String? = null

    fun loadOldRoutine() {
        if (!currentExercisesLoaded) {
            oldExercises.value?.let {
                _currentExercises = it.toMutableList()
                currentExercisesLoaded = true
            }
        }
    }

    fun deleteRoutine() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(routine.value!!)
        }
        _routineDeleted = true
    }

    fun updateRoutine() {
        if (newRoutine) return createRoutine()

        CoroutineScope(Dispatchers.IO).launch {
            dao.update(Routine(nameInputText!!, routineId))
            val oldExercises = dao.getRoutineExercises(routineId)

            if (oldExercises.size <= currentExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = currentExercises[i]
                    dao.update(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in oldExercises.size until currentExercises.size) {
                    val newExercise = currentExercises[i]
                    dao.insert(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            newExercise.exerciseId,
                            newExercise.duration
                        )
                    )
                }
            } else {
                for (i in 0 until currentExercises.size) {
                    val updatedExercise = currentExercises[i]
                    dao.update(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in currentExercises.size until oldExercises.size) {
                    dao.delete(oldExercises[i])
                }
            }
        }
    }

    private fun createRoutine() {
        CoroutineScope(Dispatchers.IO).launch {
            val newRoutineId = dao.insert(Routine(nameInputText!!))
            var order = 1

            dao.insertRoutineExercises(currentExercises.map {
                RoutineExercise(newRoutineId, order++, it.exerciseId, it.duration)
            })
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
            this.duration = newDuration
            _updatedIndex.value = exerciseIndex
        }
    }

    fun addExercise(exercise: Exercise) {
        currentExercises.add(
            RoutineExerciseName(
                exercise.id,
                exercise.name,
                DEFAULT_TIMER_DURATION
            )
        )
    }

    fun getItemDuration(index: Int): Long {
        return currentExercises[index].duration
    }
}