package ca.ramzan.virtuosity.exercises

import androidx.room.*
import ca.ramzan.virtuosity.history.SessionHistoryEntity

@Entity(tableName = "exercise_table")
data class Exercise(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    override fun toString(): String {
        return this.name
    }
}

data class ExerciseLatestBpm(
    val id: Long,
    val name: String,
    val bpm: Int?
) {
    @Ignore
    val bpmDisplay = "${bpm ?: 0} BPM"
}

@Entity(
    tableName = "exercise_history_table",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseHistory(
    @ColumnInfo(index = true)
    val exerciseId: Long,

    @ColumnInfo(index = true)
    val sessionId: Long,

    val bpm: Int,

    val time: Long = System.currentTimeMillis(),

    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0
)

data class HistoryGraphDataPoint(
    val time: Long,

    val bpm: Int
)