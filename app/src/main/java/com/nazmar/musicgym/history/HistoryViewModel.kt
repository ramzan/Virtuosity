package com.nazmar.musicgym.history

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository

class HistoryViewModel : ViewModel() {

    val history = Repository.getSessionHistory()
}