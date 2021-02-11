package com.nazmar.musicgym.data

import android.graphics.Color
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.paging.toLiveData
import com.nazmar.musicgym.db.HistoryDao
import com.nazmar.musicgym.db.SessionHistory
import com.nazmar.musicgym.db.SessionHistoryDisplay
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

    private fun toSessionHistoryDisplay(history: SessionHistory): SessionHistoryDisplay {
        return SessionHistoryDisplay(
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