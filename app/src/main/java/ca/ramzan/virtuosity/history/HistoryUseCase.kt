package ca.ramzan.virtuosity.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import ca.ramzan.virtuosity.common.room.HistoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    val history = Pager(PagingConfig(10)) {
        dao.getSessionHistories()
    }.flow
}