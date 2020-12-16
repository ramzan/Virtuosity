package com.nazmar.musicgym.exercises.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(exerciseId: Long, application: Application) :
        AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercise = dao.getExercise(exerciseId)

    private var _exerciseDeleted = MutableLiveData(false)

    val exerciseDeleted: LiveData<Boolean>
        get() = _exerciseDeleted

    fun deleteExercise() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(exercise.value!!)
        }
        _exerciseDeleted.value = true
    }

    fun renameExercise(newName: String) {
        if (newName != exercise.value!!.name) {
            CoroutineScope(Dispatchers.IO).launch {
                dao.update(Exercise(exercise.value!!.id, newName))
            }
        }
    }
}