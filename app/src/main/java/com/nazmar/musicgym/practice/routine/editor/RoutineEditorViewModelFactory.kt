package com.nazmar.musicgym.practice.routine.editor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutineEditorViewModelFactory(
    private val routineId: Long,
    private val application: Application,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineEditorViewModel::class.java)) {
            return RoutineEditorViewModel(routineId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}