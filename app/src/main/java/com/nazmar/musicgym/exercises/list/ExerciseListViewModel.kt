package com.nazmar.musicgym.exercises.list

import androidx.lifecycle.*
import com.nazmar.musicgym.data.ExerciseListUseCase
import com.nazmar.musicgym.db.ExerciseMaxBpm
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(private val useCase: ExerciseListUseCase) :
    ViewModel() {

    private var _query = MutableLiveData("")

    private val allExercises = useCase.getAllExerciseMaxBPMs().asLiveData()

    val filteredExercises: LiveData<List<ExerciseMaxBpm>> =
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
        useCase.addExercise(name)
    }
}