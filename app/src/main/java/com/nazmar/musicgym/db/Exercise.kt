package com.nazmar.musicgym.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")
data class Exercise(
        @PrimaryKey(autoGenerate = true)
        val id: Long,

        var name: String
) {
    constructor(name: String) : this(0, name)

    override fun toString(): String {
        return this.name
    }
}