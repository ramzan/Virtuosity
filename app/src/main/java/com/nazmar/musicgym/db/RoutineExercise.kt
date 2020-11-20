package com.nazmar.musicgym.db

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
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExercise(
    val routineId: Long,

    val exerciseId: Long,

    val duration: Long,

    val order: Int
)