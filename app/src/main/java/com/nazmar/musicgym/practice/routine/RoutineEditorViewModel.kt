package com.nazmar.musicgym.practice.routine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExerciseName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RoutineEditorViewModel(val routineId: Long, application: Application) : AndroidViewModel(application) {

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
        }
    }

    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        Collections.swap(_currentExercises, fromPos, toPos)
        return true
    }

}