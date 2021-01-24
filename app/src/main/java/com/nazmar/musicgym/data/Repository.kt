package com.nazmar.musicgym.data

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nazmar.musicgym.SAVED_SESSION_ID
import com.nazmar.musicgym.SAVED_SESSION_NAME
import com.nazmar.musicgym.SAVED_SESSION_TIME
import com.nazmar.musicgym.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Repository {
    private lateinit var dataSource: ExerciseDatabaseDao
    private lateinit var prefs: SharedPreferences

    fun setDataSource(dao: ExerciseDatabaseDao) {
        dataSource = dao
    }

    fun setPreferences(preferences: SharedPreferences) {
        prefs = preferences
        _sessionSaved.value = prefs.contains(SAVED_SESSION_NAME)
    }

    private var _sessionSaved = MutableLiveData(false)

    val sessionSaved: LiveData<Boolean>
        get() = _sessionSaved
    //------------------------------------------------------------------------------------

    suspend fun getSession(routineId: Long): MutableList<SessionExercise> {
        return if (prefs.contains(SAVED_SESSION_NAME)) {
            dataSource.getSavedSession()
        } else {
            dataSource.getSessionExercises(routineId).also {
                prefs.edit {
                    putString(SAVED_SESSION_NAME, dataSource.getRoutineName(routineId))
                    putLong(SAVED_SESSION_TIME, System.currentTimeMillis())
                    putLong(SAVED_SESSION_ID, routineId)
                }
                _sessionSaved.value = true
                CoroutineScope(Dispatchers.IO).launch {
                    dataSource.createSession(it)
                }
            }
        }
    }

    fun completeSession(exercises: List<SessionExercise>) {
        val time = prefs.getLong(SAVED_SESSION_TIME, System.currentTimeMillis())
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.insertHistoryItems(
                exercises
                    .filter { it.newBpm.isNotEmpty() }
                    .map { ExerciseHistory(it.exerciseId, it.newBpm.toInt(), time) }
            )
        }
        clearSavedSession()
    }

    fun clearSavedSession() {
        prefs.edit {
            remove(SAVED_SESSION_NAME)
            remove(SAVED_SESSION_TIME)
            remove(SAVED_SESSION_ID)
        }
        _sessionSaved.value = false
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.clearSavedSession()
        }
    }
    fun updateSessionState(sessionExercise: SessionExercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.update(sessionExercise)
        }
    }

    //------------------------------------------------------------------------------------

    fun getExercise(exerciseId: Long) = dataSource.getExercise(exerciseId)

    fun deleteExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.delete(exercise)
        }
    }

    fun renameExercise(exerciseId: Long, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.update(Exercise(name, exerciseId))
        }
    }

    fun addExercise(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.insert(Exercise(name))
        }
    }

    fun getAllExerciseMaxBPMs() = dataSource.getAllExerciseMaxBPMs()

    fun getAllExercises() = dataSource.getAllRoutines()

    //------------------------------------------------------------------------------------

    fun getRoutine(routineId: Long) = dataSource.getRoutine(routineId)

    fun getRoutineExerciseNames(routineId: Long) = dataSource.getRoutineExerciseNames(routineId)

    fun deleteRoutine(it: Routine) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.delete(it)
        }
    }

    fun createRoutine(routineName: String, exercises: List<RoutineExerciseName>) {
        CoroutineScope(Dispatchers.IO).launch {
            val newRoutineId = dataSource.insert(Routine(routineName))
            var order = 1

            dataSource.insertRoutineExercises(exercises.map {
                RoutineExercise(newRoutineId, order++, it.exerciseId, it.duration)
            })
        }
    }

    fun getAllRoutines() = dataSource.getAllRoutines()

    fun updateRoutine(
        routineId: Long,
        routineName: String,
        newExercises: List<RoutineExerciseName>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.update(Routine(routineName, routineId))
            val oldExercises = dataSource.getRoutineExercises(routineId)

            if (oldExercises.size <= newExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = newExercises[i]
                    dataSource.update(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in oldExercises.size until newExercises.size) {
                    val newExercise = newExercises[i]
                    dataSource.insert(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            newExercise.exerciseId,
                            newExercise.duration
                        )
                    )
                }
            } else {
                for (i in newExercises.indices) {
                    val updatedExercise = newExercises[i]
                    dataSource.update(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in newExercises.size until oldExercises.size) {
                    dataSource.delete(oldExercises[i])
                }
            }
        }

    }
}