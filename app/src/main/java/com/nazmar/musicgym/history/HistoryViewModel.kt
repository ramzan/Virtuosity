package com.nazmar.musicgym.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.SessionHistory

class HistoryViewModel : ViewModel() {
    val history: LiveData<PagedList<SessionHistory>> =
        Repository.getSessionHistory().toLiveData(pageSize = 50)
}