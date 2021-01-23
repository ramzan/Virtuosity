package com.nazmar.musicgym.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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

    val duration: Long
)

data class RoutineExerciseName(
    val exerciseId: Long,

    val name: String,

    var duration: Long
)

@Entity(tableName = "saved_session_table")
data class SessionExercise(
    val order: Int,

    val exerciseId: Long,

    val name: String,

    val duration: Long,

    val bpmRecord: Int,

    var newBpm: String = ""
)