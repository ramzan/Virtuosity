package com.nazmar.musicgym.history

import android.graphics.Color
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.paging.toLiveData
import com.nazmar.musicgym.common.room.HistoryDao
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

    fun getSessionHistory() = dao.getSessionHistories()
        .map { toSessionHistoryDisplay(it) }
        .toLiveData(pageSize = 50)

    private fun toSessionHistoryDisplay(history: SessionHistoryEntity): SessionHistory {
        return SessionHistory(
            id = history.id,
            time = history.time,
            title = history.title,
            text = buildSpannedString {
                for (i in history.exercises.indices) {
                    append("${history.exercises[i]}: ${history.bpms[i]} BPM")
                    if (history.improvements[i].isEmpty()) append("\n")
                    else {
                        color(
                            Color.GREEN
                        ) {
                            this.append(" ${history.improvements[i]}\n")
                        }
                    }
                }
            }
        )
    }
}