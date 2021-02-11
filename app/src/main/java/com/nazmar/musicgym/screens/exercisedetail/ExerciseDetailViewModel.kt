package com.nazmar.musicgym.screens.exercisedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.nazmar.musicgym.exercises.ExerciseDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ExerciseDetailViewModel @AssistedInject constructor(
    @Assisted exerciseId: Long,
    private val useCase: ExerciseDetailUseCase
) : ViewModel() {

    val exercise = useCase.getExercise(exerciseId).asLiveData()

    private var _exerciseDeleted = false

    val exerciseDeleted get() = _exerciseDeleted

    fun deleteExercise() = exercise.value?.let {
        useCase.deleteExercise(it)
        _exerciseDeleted = true
    }


    fun renameExercise(newName: String) =
        exercise.value?.let { useCase.renameExercise(it, newName) }

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