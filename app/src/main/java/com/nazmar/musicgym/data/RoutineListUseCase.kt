package com.nazmar.musicgym.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.nazmar.musicgym.SAVED_SESSION_ID
import com.nazmar.musicgym.SAVED_SESSION_NAME
import com.nazmar.musicgym.SAVED_SESSION_TIME
import com.nazmar.musicgym.db.ExerciseDatabaseDao
import javax.inject.Inject

class RoutineListUseCase @Inject constructor(
    private val dao: ExerciseDatabaseDao,
    private val prefs: SharedPreferences
) {

    fun getAllRoutines() = dao.getAllRoutines()

    fun clearSavedSession() {
        prefs.edit {
            remove(SAVED_SESSION_NAME)
            remove(SAVED_SESSION_TIME)
            remove(SAVED_SESSION_ID)
        }
    }
}