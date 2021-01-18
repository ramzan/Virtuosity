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

    var nameInputText: String? = null

    fun deleteExercise() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(exercise.value!!)
        }
        _exerciseDeleted = true
    }

    fun renameExercise() {
        if (nameInputText != exercise.value!!.name) {
            CoroutineScope(Dispatchers.IO).launch {
                dao.update(Exercise(exercise.value!!.id, nameInputText!!))
            }
        }
    }
}