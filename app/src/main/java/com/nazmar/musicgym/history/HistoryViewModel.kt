package com.nazmar.musicgym.history

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository

class HistoryViewModel : ViewModel() {

    private var itemToDelete: Long? = null

    fun setItemToDelete(id: Long) {
        itemToDelete = id
    }

    fun deleteHistoryItem() = itemToDelete?.let {
        Repository.deleteSessionHistory(it)
        itemToDelete = null
    }

    val history = Repository.getSessionHistory()
}