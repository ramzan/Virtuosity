package com.nazmar.musicgym.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")
data class Exercise(
        @PrimaryKey(autoGenerate = true)
        val id: Long,

        var name: String,

        @ColumnInfo(name = "max_bpm")
        var maxBpm: Int
) {
    constructor(name: String) : this(0, name, 0)
}