package ca.ramzan.virtuosity.session

import android.content.SharedPreferences
import androidx.core.content.edit
import ca.ramzan.virtuosity.common.SAVED_SESSION_ID
import ca.ramzan.virtuosity.common.SAVED_SESSION_NAME
import ca.ramzan.virtuosity.common.SAVED_SESSION_NOTE
import ca.ramzan.virtuosity.common.SAVED_SESSION_TIME
import ca.ramzan.virtuosity.common.room.SessionDao
import ca.ramzan.virtuosity.history.SessionHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionUseCase @Inject constructor(
    private val dao: SessionDao,
    private val prefs: SharedPreferences
) {

    suspend fun getRoutineName(id: Long) =
        prefs.getString(SAVED_SESSION_NAME, dao.getRoutineName(id)) ?: ""

    suspend fun getSession(routineId: Long): MutableList<SessionExercise> {
        return if (prefs.contains(SAVED_SESSION_NAME)) {
            dao.getSavedSession()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                dao.clearSavedSession()
            }
            dao.getSessionExercises(routineId).also {
                prefs.edit {
                    putString(SAVED_SESSION_NAME, dao.getRoutineName(routineId))
                    putLong(SAVED_SESSION_TIME, System.currentTimeMillis())
                    putLong(SAVED_SESSION_ID, routineId)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    dao.createSession(it)
                }
            }
        }
    }

    fun getNote(): String = prefs.getString(SAVED_SESSION_NOTE, "") ?: ""

    fun updateNote(newNote: String) = prefs.edit { putString(SAVED_SESSION_NOTE, newNote) }

    fun updateSessionState(sessionExercise: SessionExercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(sessionExercise)
        }
    }

    fun completeSession(exercises: List<SessionExercise>) {
        val time = prefs.getLong(SAVED_SESSION_TIME, System.currentTimeMillis())
        val title = prefs.getString(SAVED_SESSION_NAME, "") as String
        val note: String? = prefs.getString(SAVED_SESSION_NOTE, null)?.trim().run {
            if (this.isNullOrEmpty()) null else this
        }

        CoroutineScope(Dispatchers.IO).launch {
            dao.completeSession(
                exercises
                    .filter { dao.exerciseExists(it.exerciseId) },
                SessionHistoryEntity(
                    time,
                    title,
                    exercises.map { it.name },
                    exercises.map { it.newBpm },
                    exercises.map { it.getBpmDiff() },
                    note
                ),
                time
            )
        }
        clearSavedSession()
    }

    fun clearSavedSession() {
        prefs.edit {
            remove(SAVED_SESSION_NAME)
            remove(SAVED_SESSION_TIME)
            remove(SAVED_SESSION_ID)
            remove(SAVED_SESSION_NOTE)
        }
        CoroutineScope(Dispatchers.IO).launch {
            dao.clearSavedSession()
        }
    }
}