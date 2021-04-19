package ca.ramzan.virtuosity.routine

import ca.ramzan.virtuosity.common.room.RoutineEditorDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoutineEditorUseCase @Inject constructor(private val dao: RoutineEditorDao) {

    suspend fun getRoutine(id: Long) = dao.getRoutine(id)

    suspend fun getRoutineExerciseNames(routineId: Long) = dao.getRoutineExerciseNames(routineId)

    fun deleteRoutine(routine: Routine) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(routine)
        }
    }

    fun createRoutine(routineName: String, exercises: List<RoutineExercise>) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.createRoutine(routineName, exercises)
        }
    }

    fun updateRoutine(
        routineId: Long,
        routineName: String,
        newExercises: List<RoutineExercise>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val oldExercises = dao.getRoutineExercises(routineId)
            val updatedExercises = mutableListOf<RoutineExerciseEntity>()
            val deletedExercises = mutableListOf<RoutineExerciseEntity>()

            if (oldExercises.size <= newExercises.size) {
                for (i in oldExercises.indices) {
                    val updatedExercise = newExercises[i]
                    updatedExercises.add(
                        RoutineExerciseEntity(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in oldExercises.size until newExercises.size) {
                    val newExercise = newExercises[i]
                    updatedExercises.add(
                        RoutineExerciseEntity(
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
                    updatedExercises.add(
                        RoutineExerciseEntity(
                            routineId,
                            i + 1,
                            updatedExercise.exerciseId,
                            updatedExercise.duration
                        )
                    )
                }
                for (i in newExercises.size until oldExercises.size) {
                    deletedExercises.add(oldExercises[i])
                }
            }
            dao.updateRoutine(Routine(routineName, routineId), updatedExercises, deletedExercises)
        }
    }
}