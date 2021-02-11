package com.nazmar.musicgym.screens.history

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.history.HistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val useCase: HistoryUseCase) : ViewModel() {

    fun deleteHistoryItem(id: Long) = useCase.deleteSessionHistory(id)

    val history = useCase.getSessionHistory()
}