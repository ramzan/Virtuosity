package ca.ramzan.virtuosity.history

import android.graphics.Color
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "session_history_table")
data class SessionHistoryEntity(
    val time: Long,

    val title: String,

    val exercises: List<String>,

    val bpms: List<String>,

    val improvements: List<String>,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    fun toSessionHistoryDisplay(): SessionHistory {
        return SessionHistory(
            id,
            Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(formatter),
            title,
            text = buildSpannedString {
                if (exercises.isEmpty()) return@buildSpannedString
                for (i in 0 until exercises.size - 1) {
                    append("${i + 1}. ${exercises[i]}: ${bpms[i]} BPM")
                    if (improvements[i].isEmpty()) append("\n")
                    else color(Color.BLUE) { append(" ${improvements[i]}\n") }
                }
                append("${exercises.size}. ${exercises.last()}: ${bpms.last()} BPM")
                if (improvements.last().isNotEmpty()) {
                    color(Color.BLUE) { append(" ${improvements.last()}") }
                }
            }
        )
    }

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("cccc, MMMM d y, h:mm a")
    }
}

data class SessionHistory(
    val id: Long,

    val time: String,

    val title: String,

    val text: SpannedString
)