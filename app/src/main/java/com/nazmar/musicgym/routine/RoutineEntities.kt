package com.nazmar.musicgym.routine

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nazmar.musicgym.exercises.Exercise

@Entity(tableName = "routine_table")
data class Routine(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

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
data class RoutineExerciseEntity(
    val routineId: Long,

    val order: Int,

    @ColumnInfo(index = true)
    val exerciseId: Long,

    val duration: Long
)

data class RoutineExercise(
    val exerciseId: Long,

    val name: String,

    val duration: Long
)