package com.nazmar.musicgym.screens.exercisedetail

import androidx.lifecycle.*
import com.nazmar.musicgym.exercises.ExerciseDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val DURATION_WEEK = 604800000L // 7 days
const val DURATION_MONTH = 2592000000L // 30 days
const val DURATION_QUARTER = 7776000000L // 90 days
const val DURATION_YEAR = 31536000000L // 365 days

class ExerciseDetailViewModel @AssistedInject constructor(
    @Assisted private val exerciseId: Long,
    private val useCase: ExerciseDetailUseCase
) : ViewModel() {

    // Exercise -----------------------------------------------------------------------------------

    val exercise = useCase.getExercise(exerciseId).asLiveData()

    fun deleteExercise() = exercise.value?.let { useCase.deleteExercise(it) }

    fun renameExercise(newName: String) =
        exercise.value?.let { useCase.renameExercise(it, newName) }

    // History -----------------------------------------------------------------------------------

    private val _history = MutableLiveData<ExerciseDetailUseCase.GraphState>()

    val history get() = _history

    init {
        viewModelScope.launch {
            useCase.run {
                graphState.collect {
                    _history.value = it
                }
                getExerciseHistorySince(
                    exerciseId,
                    System.currentTimeMillis() - DURATION_WEEK
                )
            }
        }
    }

    fun getHistory(spinnerPosition: Int) {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                when (spinnerPosition) {
                    0 -> System.currentTimeMillis() - DURATION_WEEK
                    1 -> System.currentTimeMillis() - DURATION_MONTH
                    2 -> System.currentTimeMillis() - DURATION_QUARTER
                    3 -> System.currentTimeMillis() - DURATION_YEAR
                    4 -> 0L
                    else -> throw Exception("Illegal spinner position: $spinnerPosition")
                }
            )
        }

    }

    // Factory -----------------------------------------------------------------------------------

    @AssistedFactory
    interface Factory {
        fun create(exerciseId: Long): ExerciseDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            exerciseId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(exerciseId) as T
            }
        }
    }
}