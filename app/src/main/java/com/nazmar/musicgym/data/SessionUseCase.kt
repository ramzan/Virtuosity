package com.nazmar.musicgym.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.nazmar.musicgym.SAVED_SESSION_ID
import com.nazmar.musicgym.SAVED_SESSION_NAME
import com.nazmar.musicgym.SAVED_SESSION_TIME
import com.nazmar.musicgym.db.ExerciseDatabaseDao
import com.nazmar.musicgym.db.SessionExercise
import com.nazmar.musicgym.db.SessionHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionUseCase @Inject constructor(
    private val dao: ExerciseDatabaseDao,
    private val prefs: SharedPreferences
) {

    suspend fun getRoutine(id: Long) = dao.getRoutine(id)

    suspend fun getSession(routineId: Long): MutableList<SessionExercise> {
        return if (prefs.contains(SAVED_SESSION_NAME)) {
            dao.getSavedSession()
        } else {
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

    fun updateSessionState(sessionExercise: SessionExercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(sessionExercise)
        }
    }

    fun completeSession(exercises: List<SessionExercise>) {
        val time = prefs.getLong(SAVED_SESSION_TIME, System.currentTimeMillis())
        val title = prefs.getString(SAVED_SESSION_NAME, "") as String

        CoroutineScope(Dispatchers.IO).launch {
            dao.completeSession(
                exercises
                    .filter { dao.exerciseExists(it.exerciseId) },
                SessionHistory(
                    time,
                    title,
                    exercises.map { it.name },
                    exercises.map { it.newBpm },
                    exercises.map { it.getBpmDiff() }
                ),
                time
            )
        }
        clearSavedSession()
    }

    private fun clearSavedSession() {
        prefs.edit {
            remove(SAVED_SESSION_NAME)
            remove(SAVED_SESSION_TIME)
            remove(SAVED_SESSION_ID)
        }
        CoroutineScope(Dispatchers.IO).launch {
            dao.clearSavedSession()
        }
    }
}