package com.nazmar.musicgym.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class Routine(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)