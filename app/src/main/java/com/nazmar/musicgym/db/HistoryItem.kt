package com.nazmar.musicgym.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_table",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long,

    @ColumnInfo(index = true)
    val exerciseId: Long,

    val time: Long,

    val bpm: Int
) {
    constructor(exerciseId: Long, bpm: Int) : this(
        itemId = 0,
        exerciseId = exerciseId,
        time = System.currentTimeMillis(),
        bpm = bpm
    )
}

data class ExerciseMaxBpm(
    val id: Long,
    val name: String,
    val bpm: Int?
)