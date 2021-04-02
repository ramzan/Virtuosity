package ca.ramzan.virtuosity.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    val displayTime: String
        get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(formatter)

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d y, h:mm a")
    }
}