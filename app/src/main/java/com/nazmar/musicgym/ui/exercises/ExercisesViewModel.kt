package com.nazmar.musicgym.ui.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.ExerciseMaxBpm
import com.nazmar.musicgym.respository.Repository
import java.util.*

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository(ExerciseDatabase.getInstance(application))

    val exercises: LiveData<List<ExerciseMaxBpm>>
        get() = getFilteredExercises()

    private var _query = MutableLiveData("")

    private fun getFilteredExercises(): LiveData<List<ExerciseMaxBpm>> {
        return Transformations.switchMap(_query) { name: String ->
            repo.getFilteredExerciseMaxBPMs(name)
        }
    }

    fun setNameQuery(name: String) {
        _query.value = name.toLowerCase(Locale.ROOT).trim()
    }

    fun addExercise(name: String) {
        repo.addExercise(Exercise(name))
    }
}