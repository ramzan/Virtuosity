package ca.ramzan.virtuosity.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import ca.ramzan.virtuosity.common.DateFormatter

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
    val displayTime: String get() = DateFormatter.fromMilli(time)
}