package com.nazmar.musicgym.data

import com.nazmar.musicgym.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Repository {
    private lateinit var dataSource: ExerciseDatabaseDao

    fun setDataSource(dao: ExerciseDatabaseDao) {
        dataSource = dao
    }

    //------------------------------------------------------------------------------------

    suspend fun getSession(sessionId: Long, restore: Boolean): MutableList<SessionExercise> {
        return if (restore) {
            dataSource.getSavedSession()
        } else {
            dataSource.getSessionExercises(sessionId).also {
                CoroutineScope(Dispatchers.IO).launch {
                    dataSource.createSession(it)
                }
            }
        }
    }

    fun completeSession(exercises: MutableList<SessionExercise>) {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.insertHistoryItems(
                exercises
                    .filter { it.newBpm.isNotEmpty() }
                    .map { HistoryItem(it.exerciseId, it.newBpm.toInt()) }
            )
            dataSource.clearSavedSession()
        }
    }

    fun clearSavedSession() {
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.clearSavedSession()
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