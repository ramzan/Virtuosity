package com.nazmar.musicgym.practice.routine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExercise
import com.nazmar.musicgym.db.RoutineExerciseName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RoutineEditorViewModel(private val routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val newRoutine = routineId == 0L

    val routine = dao.getRoutine(routineId)

    val exercises = dao.getAllExercises()

    val oldExercises = dao.getRoutineExerciseNames(routineId)

    private var _currentExercises = mutableListOf<RoutineExerciseName>()

    val currentExercises: MutableList<RoutineExerciseName>
        get() = _currentExercises

    fun loadOldRoutine() {
        _currentExercises = oldExercises.value!!.toMutableList()
    }

    fun deleteRoutine() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(routine.value!!)
        }
    }

    fun updateRoutine(newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(Routine(routineId, newName))
            val oldExercises = dao.getRoutineExercises(routineId)

            if (oldExercises.size <= currentExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = currentExercises[i]
                    dao.update(RoutineExercise(routineId, i + 1, updatedExercise.exerciseId, updatedExercise.duration))
                }
                for (i in oldExercises.size until currentExercises.size) {
                    val newExercise = currentExercises[i]
                    dao.insert(RoutineExercise(routineId, i + 1, newExercise.exerciseId, newExercise.duration))
                }
            } else {
                for (i in 0 until currentExercises.size) {
                    val updatedExercise = currentExercises[i]
                    dao.update(RoutineExercise(routineId, i + 1, updatedExercise.exerciseId, updatedExercise.duration))
                }
                for (i in currentExercises.size until oldExercises.size) {
                    dao.delete(oldExercises[i])
                }
            }
        }
    }


    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        Collections.swap(_currentExercises, fromPos, toPos)
        return true
    }

    fun deleteItem(index: Int) {
        _currentExercises.removeAt(index)
    }

}