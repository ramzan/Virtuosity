package ca.ramzan.virtuosity.routine

import android.content.SharedPreferences
import androidx.core.content.edit
import ca.ramzan.virtuosity.common.SAVED_SESSION_ID
import ca.ramzan.virtuosity.common.SAVED_SESSION_NAME
import ca.ramzan.virtuosity.common.SAVED_SESSION_TIME
import ca.ramzan.virtuosity.common.room.RoutineListDao
import javax.inject.Inject

class RoutineListUseCase @Inject constructor(
    private val dao: RoutineListDao,
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