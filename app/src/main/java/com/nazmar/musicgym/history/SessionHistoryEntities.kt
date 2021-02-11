package com.nazmar.musicgym.history

import android.text.SpannedString
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_history_table")
data class SessionHistoryEntity(
    val time: Long,

    val title: String,

    val exercises: List<String>,

    val bpms: List<String>,

    val improvements: List<String>,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

data class SessionHistory(
    val id: Long,

    val time: Long,

    val title: String,

    val text: SpannedString
)