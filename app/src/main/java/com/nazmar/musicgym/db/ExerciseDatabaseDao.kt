package com.nazmar.musicgym.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExerciseDatabaseDao {

    @Insert
    suspend fun insert(exercise: Exercise)

    @Insert
    suspend fun insert(exercises: List<Exercise>)

    @Insert
    suspend fun insert(historyItem: HistoryItem)

    @Insert
    suspend fun insert(routineExercise: RoutineExercise)

    @Insert
    suspend fun insert(routine: Routine)

    @Update
    suspend fun update(exercise: Exercise)

    @Update
    suspend fun update(routine: Routine)

    @Update
    suspend fun update(routineExercise: RoutineExercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Insert
    suspend fun delete(historyItem: HistoryItem)

    @Insert
    suspend fun delete(routineExercise: RoutineExercise)

    @Insert
    suspend fun delete(routine: Routine)

    @Query("SELECT * FROM exercise_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): LiveData<List<Exercise>>

}