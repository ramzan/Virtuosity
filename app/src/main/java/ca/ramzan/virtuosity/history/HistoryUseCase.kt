package ca.ramzan.virtuosity.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import ca.ramzan.virtuosity.common.room.HistoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryUseCase @Inject constructor(private val dao: HistoryDao) {

    fun deleteSessionHistory(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.getSessionHistory(id)?.let {
                dao.delete(it)
            }
        }
    }

    val history = Pager(PagingConfig(50)) {
        dao.getSessionHistories()
    }.flow.map { pagingData ->
        pagingData.map { it.toSessionHistoryDisplay() }
    }
}