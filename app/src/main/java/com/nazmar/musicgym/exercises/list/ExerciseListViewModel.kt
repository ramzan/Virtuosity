package com.nazmar.musicgym.exercises.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.ExerciseMaxBpm
import java.util.*

class ExerciseListViewModel : ViewModel() {

    private var _query = MutableLiveData("")

    val exercises: LiveData<List<ExerciseMaxBpm>> =
        Transformations.switchMap(_query) { name: String ->
            getFilteredExerciseMaxBPMs(name)
        }

    private fun getFilteredExerciseMaxBPMs(query: String): LiveData<List<ExerciseMaxBpm>> {
        return Transformations.map(Repository.getAllExerciseMaxBPMs()) {
            it.filter { exercise -> exercise.name.toLowerCase(Locale.ROOT).contains(query) }
        }
    }

    fun setNameQuery(name: String) {
        _query.value = name.toLowerCase(Locale.ROOT).trim()
    }

    fun addExercise(name: String) {
        Repository.addExercise(name)
    }
}