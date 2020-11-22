package com.nazmar.musicgym.db

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
class HistoryItem(
        @PrimaryKey(autoGenerate = true)
        val id: Long,

        val exerciseId: Long,

        val time: Long = System.currentTimeMillis(),

        val bpm: Int,

        val note: String
) {
    constructor(exerciseId: Long, bpm: Int, note: String) : this(
            id = 0,
            exerciseId = exerciseId,
            bpm = bpm,
            note = note
    )
}