package com.nazmar.musicgym.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExerciseDatabaseDao {

    @Insert
    suspend fun insert(exercise: Exercise)

    @Insert
    suspend fun insertExercises(exercises: List<Exercise>)

    @Insert
    suspend fun insert(historyItem: HistoryItem)

    @Insert
    suspend fun insertHistoryItems(historyItems: List<HistoryItem>)

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

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    fun getExercise(key: Long): LiveData<Exercise?>

    @Query("SELECT id, name, MAX(bpm) AS bpm FROM exercise_table LEFT OUTER JOIN history_table ON exerciseId = id GROUP BY id ORDER BY name COLLATE NOCASE ASC")
    fun getAllExerciseMaxBPMs(): LiveData<List<ExerciseMaxBpm>>

    @Query("SELECT * FROM routine_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllRoutines(): LiveData<List<Routine>>

    @Query("SELECT * FROM routine_exercise_table WHERE routineId = :routineId ORDER BY `order` COLLATE NOCASE ASC")
    fun getRoutineExercises(routineId: Long): LiveData<List<RoutineExercise>>
}