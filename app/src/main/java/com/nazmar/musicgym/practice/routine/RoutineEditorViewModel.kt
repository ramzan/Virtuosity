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

class RoutineEditorViewModel(val routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val newRoutine = routineId == 0L

    val routine = dao.getRoutine(routineId)

    val routineExercises = dao.getRoutineExerciseNames(routineId)

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

    fun move(fromPos: Int, toPos: Int) : List<RoutineExerciseName>{
        val ls = routineExercises.value!!.toMutableList()
        val tmp = ls[fromPos]
        ls[fromPos] = ls[toPos]
        ls[toPos] = tmp
        return ls
    }
}