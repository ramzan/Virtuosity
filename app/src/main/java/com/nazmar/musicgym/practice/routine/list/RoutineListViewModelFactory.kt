package com.nazmar.musicgym.practice.routine.list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutineListViewModelFactory(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineListViewModel::class.java)) {
            return RoutineListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
