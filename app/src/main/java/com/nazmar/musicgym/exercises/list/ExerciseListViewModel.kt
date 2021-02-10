package com.nazmar.musicgym.exercises.list

import androidx.lifecycle.*
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.ExerciseMaxBpm
import java.util.*

class ExerciseListViewModel : ViewModel() {

    private var _query = MutableLiveData("")

    private val allExercises = Repository.getAllExerciseMaxBPMs().asLiveData()

    val exercises: LiveData<List<ExerciseMaxBpm>> =
        Transformations.switchMap(_query) { name: String ->
            getFilteredExerciseMaxBPMs(name)
        }

    private fun getFilteredExerciseMaxBPMs(query: String): LiveData<List<ExerciseMaxBpm>> {
        return Transformations.map(allExercises) {
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