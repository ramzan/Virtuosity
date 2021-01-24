package com.nazmar.musicgym.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_history_table",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseHistory(
    @ColumnInfo(index = true)
    val exerciseId: Long,

    val bpm: Int,

    val time: Long = System.currentTimeMillis(),

    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0
)

@Entity(tableName = "session_history_table")
data class SessionHistory(
    val time: Long,

    val title: String,

    val exercises: List<String>,

    val bpms: List<String>,

    val improvements: List<String>,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)