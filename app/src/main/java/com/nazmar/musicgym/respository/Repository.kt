package com.nazmar.musicgym.respository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.Exercise
import com.nazmar.musicgym.db.ExerciseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Repository(database: ExerciseDatabase) {

    private val db = database.exerciseDatabaseDao

    fun getAllExercises(): LiveData<List<Exercise>> {
        return db.getAllExercises()
    }

    fun getFilteredExercises(query: String): LiveData<List<Exercise>> {
        return Transformations.map(getAllExercises()) {
            it.filter { exercise -> exercise.name.toLowerCase(Locale.ROOT).contains(query) }
        }
    }

    fun addExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            insert(exercise)
        }
    }

    fun updateExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            update(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            delete(exercise)
        }
    }


    private suspend fun insert(exercise: Exercise) {
        db.insert(exercise)
    }


    private suspend fun update(exercise: Exercise) {
        db.update(exercise)
    }

    private suspend fun delete(exercise: Exercise) {
        return db.delete(exercise)
    }
}
