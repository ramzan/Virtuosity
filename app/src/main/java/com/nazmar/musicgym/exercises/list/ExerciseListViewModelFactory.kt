package com.nazmar.musicgym.exercises.list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseListViewModelFactory(
        private val application: Application
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseListViewModel::class.java)) {
            return ExerciseListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
