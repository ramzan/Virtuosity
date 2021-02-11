package com.nazmar.musicgym.data

import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDetailDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseDetailUseCase @Inject constructor(private val dao: ExerciseDetailDao) {

    fun getExercise(id: Long) = dao.getExercise(id)

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