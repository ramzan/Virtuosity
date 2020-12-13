package com.nazmar.musicgym.practice

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PracticeViewModelFactory(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
            return PracticeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
