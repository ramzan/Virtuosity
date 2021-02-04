package com.nazmar.musicgym.routine.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutineEditorViewModelFactory(private val routineId: Long) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineEditorViewModel::class.java)) {
            return RoutineEditorViewModel(routineId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}