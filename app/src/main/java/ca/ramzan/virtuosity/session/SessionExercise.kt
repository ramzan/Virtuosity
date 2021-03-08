package ca.ramzan.virtuosity.session

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.sign


@Entity(tableName = "saved_session_table")
data class SessionExercise(
    @PrimaryKey
    val order: Int,

    val exerciseId: Long,

    val name: String,

    val duration: Long,

    val bpmRecord: Int,

    val newBpm: String = ""
) {
    fun getBpmDiff() = (newBpm.toInt() - bpmRecord).let { diff ->
        when (diff.sign) {
            1 -> "+$diff"
            else -> ""
        }
    }
}