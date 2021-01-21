package com.nazmar.musicgym.exercises.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(exerciseId: Long, application: Application) :
    AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercise = dao.getExercise(exerciseId)

    private var _exerciseDeleted = false

    val exerciseDeleted: Boolean
        get() = _exerciseDeleted

    var nameInputText: String = ""

    fun deleteExercise() {
        exercise.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                dao.delete(it)
            }
            _exerciseDeleted = true
        }
    }

    fun renameExercise() {
        exercise.value?.let { exercise ->
            if (nameInputText != exercise.name) {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.update(Exercise(nameInputText, exercise.id))
                }
            }

        }
    }
}