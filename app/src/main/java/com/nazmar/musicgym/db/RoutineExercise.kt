package com.nazmar.musicgym.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Duration
import kotlin.math.sign

@Entity(
    tableName = "routine_exercise_table",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["routineId", "order"]
)
data class RoutineExercise(
    val routineId: Long,

    val order: Int,

    @ColumnInfo(index = true)
    val exerciseId: Long,

    val duration: Duration
)

data class RoutineExerciseName(
    val exerciseId: Long,

    val name: String,

    var duration: Duration
)

@Entity(tableName = "saved_session_table")
data class SessionExercise(
    @PrimaryKey
    val order: Int,

    val exerciseId: Long,

    val name: String,

    val duration: Duration,

    val bpmRecord: Int,

    var newBpm: String = ""
) {
    fun getBpmDiff() = (newBpm.toInt() - bpmRecord).let { diff ->
        when (diff.sign) {
            1 -> "+$diff"
            else -> ""
        }
    }
}