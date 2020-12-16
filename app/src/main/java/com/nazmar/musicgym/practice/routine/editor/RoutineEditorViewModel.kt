package com.nazmar.musicgym.practice.routine.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExercise
import com.nazmar.musicgym.db.RoutineExerciseName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RoutineEditorViewModel(private val routineId: Long, application: Application) :
        AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val newRoutine = routineId == 0L

    val routine = dao.getRoutine(routineId)

    val exercises = dao.getAllExercises()

    val oldExercises = dao.getRoutineExerciseNames(routineId)

    private var _currentExercises = mutableListOf<RoutineExerciseName>()

    val currentExercises: MutableList<RoutineExerciseName>
        get() = _currentExercises

    private var _updatedIndex = MutableLiveData<Int>()

    val updatedIndex: LiveData<Int>
        get() = _updatedIndex

    fun loadOldRoutine() {
        _currentExercises = oldExercises.value!!.toMutableList()
    }

    fun deleteRoutine() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(routine.value!!)
        }
    }

    fun updateRoutine(newName: String) {
        if (newRoutine) return createRoutine(newName)

        CoroutineScope(Dispatchers.IO).launch {
            dao.update(Routine(routineId, newName))
            val oldExercises = dao.getRoutineExercises(routineId)

            if (oldExercises.size <= currentExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = currentExercises[i]
                    dao.update(
                            RoutineExercise(
                                    routineId,
                                    i + 1,
                                    updatedExercise.exerciseId,
                                    updatedExercise.getDuration()
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
                                    newExercise.getDuration()
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
                                    updatedExercise.getDuration()
                            )
                    )
                }
                for (i in currentExercises.size until oldExercises.size) {
                    dao.delete(oldExercises[i])
                }
            }
        }
    }

    private fun createRoutine(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val newRoutineId = dao.insert(Routine(name))
            var order = 1

            dao.insertRoutineExercises(currentExercises.map {
                RoutineExercise(newRoutineId, order++, it.exerciseId, it.getDuration())
            })
        }
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        Collections.swap(_currentExercises, fromPos, toPos)
        return true
    }

    fun deleteItem(index: Int) {
        _currentExercises.removeAt(index)
    }

    fun updateDuration(exerciseIndex: Int, minutes: Long, seconds: Long) {
        with(currentExercises[exerciseIndex]) {
            this.minutes = minutes
            this.seconds = seconds
            _updatedIndex.value = exerciseIndex
        }
    }

    fun addExercise(index: Int) {
        exercises.value!![index].apply {
            currentExercises.add(RoutineExerciseName(this.id, this.name, 5, 0))
        }
    }

    fun getItemDuration(index: Int): Long {
        return currentExercises[index].getDuration()
    }
}