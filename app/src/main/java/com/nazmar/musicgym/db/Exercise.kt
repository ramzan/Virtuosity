package com.nazmar.musicgym.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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

data class ExerciseMaxBpm(
    val id: Long,
    val name: String,
    val bpm: Int?
)