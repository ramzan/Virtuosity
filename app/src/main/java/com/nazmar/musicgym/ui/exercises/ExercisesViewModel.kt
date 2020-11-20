package com.nazmar.musicgym.ui.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.respository.Repository

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository(ExerciseDatabase.getInstance(application))

    // List of exercises to display
    private val _exercises = repo.getAllExercises()

    val exercises: LiveData<List<Exercise>>
        get() = _exercises

    fun addExercise() {
        repo.addExercise(Exercise("butt"))
    }
}