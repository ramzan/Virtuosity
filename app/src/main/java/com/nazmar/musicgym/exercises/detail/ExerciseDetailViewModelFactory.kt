package com.nazmar.musicgym.exercises.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseDetailViewModelFactory(private val exerciseId: Long) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            return ExerciseDetailViewModel(exerciseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}