package com.nazmar.musicgym.data

import com.nazmar.musicgym.db.ExerciseDatabaseDao
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExercise
import com.nazmar.musicgym.db.RoutineExerciseName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoutineEditorUseCase @Inject constructor(private val dao: ExerciseDatabaseDao) {

    fun getAllExercises() = dao.getAllExercises()

    suspend fun getRoutine(id: Long) = dao.getRoutine(id)

    suspend fun getRoutineExerciseNames(routineId: Long) = dao.getRoutineExerciseNames(routineId)

    fun deleteRoutine(routine: Routine) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(routine)
        }
    }

    fun createRoutine(routineName: String, exercises: List<RoutineExerciseName>) {
        CoroutineScope(Dispatchers.IO).launch {
            val newRoutineId = dao.insert(Routine(routineName))
            var order = 1
            dao.insertRoutineExercises(exercises.map {
                RoutineExercise(newRoutineId, order++, it.exerciseId, it.duration)
            })
        }
    }

    fun updateRoutine(
        routineId: Long,
        routineName: String,
        newExercises: List<RoutineExerciseName>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(Routine(routineName, routineId))
            val oldExercises = dao.getRoutineExercises(routineId)

            if (oldExercises.size <= newExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = newExercises[i]
                    dao.update(
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
                    dao.insert(
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
                    dao.update(
                        RoutineExercise(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in newExercises.size until oldExercises.size) {
                    dao.delete(oldExercises[i])
                }
            }
        }

    }
}