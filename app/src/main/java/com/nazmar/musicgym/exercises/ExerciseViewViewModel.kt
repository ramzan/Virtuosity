package com.nazmar.musicgym.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewViewModel(exerciseId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercise = dao.getExercise(exerciseId)

    fun deleteExercise() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(exercise.value!!)
        }
    }

    fun renameExercise(newName: String) {
        if (newName != exercise.value!!.name) {
            CoroutineScope(Dispatchers.IO).launch {
                dao.update(Exercise(exercise.value!!.id, newName))
            }
        }
    }
}