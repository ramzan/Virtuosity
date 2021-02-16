package com.nazmar.musicgym.exercises

import com.github.mikephil.charting.data.Entry
import com.nazmar.musicgym.common.room.ExerciseDetailDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseDetailUseCase @Inject constructor(private val dao: ExerciseDetailDao) {

    fun getExercise(id: Long) = dao.getExercise(id)

    suspend fun getExerciseHistorySince(exerciseId: Long, startTime: Long) =
        dao.getExerciseHistorySince(exerciseId, startTime).map {
            Entry(it.time.toFloat(), it.bpm.toFloat())
        }

    fun renameExercise(exercise: Exercise, newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(exercise.copy(name = newName))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(exercise)
        }
    }
}