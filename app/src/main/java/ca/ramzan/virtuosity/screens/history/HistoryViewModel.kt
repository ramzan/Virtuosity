package ca.ramzan.virtuosity.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ca.ramzan.virtuosity.history.HistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val useCase: HistoryUseCase) : ViewModel() {

    var pendingDeleteId: Long? = null

    fun deleteHistoryItem() = pendingDeleteId?.let { useCase.deleteSessionHistory(it) }

    val history = useCase.history.cachedIn(viewModelScope)
}