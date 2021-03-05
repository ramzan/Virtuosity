package com.nazmar.musicgym.screens.exerciselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nazmar.musicgym.exercises.ExerciseListUseCase
import com.nazmar.musicgym.exercises.ExerciseMaxBpm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(private val useCase: ExerciseListUseCase) :
    ViewModel() {
    private val query = MutableStateFlow("")

    private val allExercises = MutableStateFlow<List<ExerciseMaxBpm>>(emptyList())

    val filteredExercises = allExercises.combine(query) { list, query ->
        list.filter { exercise ->
            exercise.name.toLowerCase(Locale.ROOT).contains(query)
        }
    }

    init {
        viewModelScope.launch {
            useCase.getAllExerciseMaxBPMs().collect {
                allExercises.emit(it)
            }
        }
    }

    fun setNameQuery(name: String) {
        query.value = name.toLowerCase(Locale.ROOT).trim()
    }

    fun addExercise(name: String) = useCase.addExercise(name)
}