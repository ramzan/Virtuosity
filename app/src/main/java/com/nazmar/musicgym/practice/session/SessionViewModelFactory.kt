package com.nazmar.musicgym.practice.session

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SessionViewModelFactory(
    private val routineId: Long,
    private val application: Application,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(routineId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}