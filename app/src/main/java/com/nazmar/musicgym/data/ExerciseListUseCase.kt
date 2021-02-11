package com.nazmar.musicgym.data

import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseListDao
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