package com.nazmar.musicgym.practice.routine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutineListViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineListViewModel::class.java)) {
            return RoutineListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
