package ca.ramzan.virtuosity.history

import android.graphics.Color
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.room.Entity
import androidx.room.PrimaryKey

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
            time,
            title,
            text = buildSpannedString {
                for (i in exercises.indices) {
                    append("${exercises[i]}: ${bpms[i]} BPM")
                    if (improvements[i].isEmpty()) append("\n")
                    else {
                        color(
                            Color.GREEN
                        ) {
                            this.append(" ${improvements[i]}\n")
                        }
                    }
                }
            }
        )
    }
}

data class SessionHistory(
    val id: Long,

    val time: Long,

    val title: String,

    val text: SpannedString
)