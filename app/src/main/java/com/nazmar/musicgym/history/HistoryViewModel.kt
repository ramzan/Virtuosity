package com.nazmar.musicgym.history

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository

class HistoryViewModel : ViewModel() {

    fun deleteHistoryItem(id: Long) = Repository.deleteSessionHistory(id)

    val history = Repository.getSessionHistory()
}