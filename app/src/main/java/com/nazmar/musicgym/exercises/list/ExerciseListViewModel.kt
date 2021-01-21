package com.nazmar.musicgym.exercises.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.ExerciseMaxBpm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ExerciseListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    private var _query = MutableLiveData("")

    val exercises: LiveData<List<ExerciseMaxBpm>> =
        Transformations.switchMap(_query) { name: String ->
            getFilteredExerciseMaxBPMs(name)
        }

    private fun getFilteredExerciseMaxBPMs(query: String): LiveData<List<ExerciseMaxBpm>> {
        return Transformations.map(dao.getAllExerciseMaxBPMs()) {
            it.filter { exercise -> exercise.name.toLowerCase(Locale.ROOT).contains(query) }
        }
    }

    fun setNameQuery(name: String) {
        _query.value = name.toLowerCase(Locale.ROOT).trim()
    }

    fun addExercise(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(Exercise(name))
        }
    }
}