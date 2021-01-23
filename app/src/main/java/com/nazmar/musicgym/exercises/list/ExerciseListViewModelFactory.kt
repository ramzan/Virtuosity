package com.nazmar.musicgym.exercises.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseListViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseListViewModel::class.java)) {
            return ExerciseListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
