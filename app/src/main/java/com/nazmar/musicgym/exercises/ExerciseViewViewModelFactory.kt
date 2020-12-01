package com.nazmar.musicgym.exercises

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseViewViewModelFactory(
        private val exerciseId: Long,
        private val application: Application,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewViewModel::class.java)) {
            return ExerciseViewViewModel(exerciseId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}