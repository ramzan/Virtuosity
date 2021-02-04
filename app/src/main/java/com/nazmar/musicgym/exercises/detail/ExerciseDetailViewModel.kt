package com.nazmar.musicgym.exercises.detail

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository

class ExerciseDetailViewModel(exerciseId: Long) : ViewModel() {

    val exercise = Repository.getExercise(exerciseId)

    private var _exerciseDeleted = false

    val exerciseDeleted: Boolean
        get() = _exerciseDeleted

    fun deleteExercise() {
        exercise.value?.let {
            Repository.deleteExercise(it)
            _exerciseDeleted = true
        }
    }

    fun renameExercise(newName: String) {
        exercise.value?.let { exercise ->
            if (newName != exercise.name) {
                Repository.renameExercise(exercise.id, newName)
            }
        }
    }
}