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
        @ColumnInfo(index = true)
        val exerciseId: Long,

        val bpm: Int,

        val time: Long = System.currentTimeMillis(),

        @PrimaryKey(autoGenerate = true)
        val itemId: Long = 0
)

data class ExerciseMaxBpm(
        val id: Long,
        val name: String,
        val bpm: Int?
)