package com.nazmar.musicgym.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.SessionHistory

class HistoryViewModel : ViewModel() {

    private var itemToDelete: SessionHistory? = null

    fun setItemToDelete(history: SessionHistory) {
        itemToDelete = history
    }

    fun deleteHistoryItem() = itemToDelete?.let { Repository.deleteSessionHistory(it) }

    val history: LiveData<PagedList<SessionHistory>> =
        Repository.getSessionHistory().toLiveData(pageSize = 50)
}