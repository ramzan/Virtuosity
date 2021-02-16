package com.nazmar.musicgym.screens.exercisedetail

import androidx.lifecycle.*
import com.github.mikephil.charting.data.Entry
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

    private var _exerciseDeleted = false

    val exerciseDeleted get() = _exerciseDeleted

    fun deleteExercise() = exercise.value?.let {
        useCase.deleteExercise(it)
        _exerciseDeleted = true
    }

    fun renameExercise(newName: String) =
        exercise.value?.let { useCase.renameExercise(it, newName) }

    // History -----------------------------------------------------------------------------------

    private val _history = MutableLiveData<ExerciseDetailUseCase.GraphState>()

    val history get() = _history

    init {
        viewModelScope.launch {
            useCase.graphState.collect {
                _history.value = it
            }
        }
    }

    fun getWeekHistory() {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                System.currentTimeMillis() - DURATION_WEEK
            )
        }
    }

    fun getMonthHistory() {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                System.currentTimeMillis() - DURATION_MONTH
            )
        }
    }

    fun getQuarterHistory() {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                System.currentTimeMillis() - DURATION_QUARTER
            )
        }
    }

    fun getYearHistory() {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                System.currentTimeMillis() - DURATION_YEAR
            )
        }
    }

    fun getAllHistory() {
        viewModelScope.launch {
            useCase.getExerciseHistorySince(
                exerciseId,
                0L
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

data class HistoryGraphData(
    val points: List<Entry>,
    val maxBpm: Float
)