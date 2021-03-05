package com.nazmar.musicgym.screens.exerciselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nazmar.musicgym.exercises.ExerciseListUseCase
import com.nazmar.musicgym.exercises.ExerciseMaxBpm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(private val useCase: ExerciseListUseCase) :
    ViewModel() {

    private var query = ""

    val state = MutableStateFlow<ExerciseListState>(ExerciseListState.Loading)

    init {
        viewModelScope.launch {
            useCase.getAllExerciseMaxBPMs().collect { list ->
                state.emit(ExerciseListState.Loaded(list, query))
            }
        }
    }

    fun setNameQuery(name: String) {
        query = name.toLowerCase(Locale.ROOT).trim()
        viewModelScope.launch {
            (state.value as? ExerciseListState.Loaded)?.run {
                state.emit(copy(query = query))
            }
        }
    }

    fun addExercise(name: String) = useCase.addExercise(name)
}

sealed class ExerciseListState {
    object Loading : ExerciseListState()

    data class Loaded(private val allExercises: List<ExerciseMaxBpm>, private val query: String) :
        ExerciseListState() {

        val filteredExercises = allExercises.filter { exercise ->
            exercise.name.toLowerCase(Locale.ROOT).contains(query)
        }
    }
}