package ca.ramzan.virtuosity.screens.exercise_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.exercises.ExerciseDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
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

    val exercise = MutableStateFlow<ExerciseDetailState>(ExerciseDetailState.Loading)

    init {
        viewModelScope.launch {
            useCase.getExercise(exerciseId).collect { e ->
                exercise.emit(e?.let {
                    ExerciseDetailState.Loaded(it)
                } ?: ExerciseDetailState.Deleted)
            }
        }
    }

    fun deleteExercise() {
        (exercise.value as? ExerciseDetailState.Loaded)?.run {
            useCase.deleteExercise(this.exercise)
        }
    }

    fun renameExercise(newName: String) {
        (exercise.value as? ExerciseDetailState.Loaded)?.run {
            useCase.renameExercise(
                this.exercise,
                newName
            )
        }
    }

    // History -----------------------------------------------------------------------------------

    val history = useCase.graphState

    private var oldPosition = -1

    fun getHistory(spinnerPosition: Int) {
        if (spinnerPosition == oldPosition) return
        oldPosition = spinnerPosition
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

sealed class ExerciseDetailState {
    object Loading : ExerciseDetailState()

    object Deleted : ExerciseDetailState()

    data class Loaded(val exercise: Exercise) : ExerciseDetailState()
}