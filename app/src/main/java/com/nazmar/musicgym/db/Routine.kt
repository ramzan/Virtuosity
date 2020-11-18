package com.nazmar.musicgym.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String
) {
    constructor(name: String) : this(0, name)
}