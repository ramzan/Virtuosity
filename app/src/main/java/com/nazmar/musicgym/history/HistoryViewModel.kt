package com.nazmar.musicgym.history

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.HistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val useCase: HistoryUseCase) : ViewModel() {

    fun deleteHistoryItem(id: Long) = useCase.deleteSessionHistory(id)

    val history = useCase.getSessionHistory()
}