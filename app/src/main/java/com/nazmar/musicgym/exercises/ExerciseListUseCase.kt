package com.nazmar.musicgym.exercises

import com.nazmar.musicgym.common.room.ExerciseListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseListUseCase @Inject constructor(private val dao: ExerciseListDao) {

    fun getAllExerciseMaxBPMs() = dao.getAllExerciseMaxBPMs()

    fun addExercise(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(Exercise(name))
        }
    }
}