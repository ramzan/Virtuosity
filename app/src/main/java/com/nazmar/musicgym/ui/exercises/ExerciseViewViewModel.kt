package com.nazmar.musicgym.ui.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.respository.Repository

class ExerciseViewViewModel(exerciseId: Long, application: Application) : AndroidViewModel(application) {

    private val repo = Repository(ExerciseDatabase.getInstance(application))

    val exercise = repo.getExercise(exerciseId)

    fun deleteExercise() {
        repo.deleteExercise(exercise.value!!)
    }

    fun renameExercise(newName: String) {
        if (newName != exercise.value!!.name) {
            repo.updateExercise(Exercise(exercise.value!!.id, newName))
        }
    }
}