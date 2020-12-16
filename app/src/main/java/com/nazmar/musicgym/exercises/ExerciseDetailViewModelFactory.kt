package com.nazmar.musicgym.exercises

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseDetailViewModelFactory(
    private val exerciseId: Long,
    private val application: Application,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            return ExerciseDetailViewModel(exerciseId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}