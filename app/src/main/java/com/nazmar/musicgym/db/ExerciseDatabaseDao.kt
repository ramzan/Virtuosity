package com.nazmar.musicgym.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExerciseDatabaseDao {

    // Insert

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
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExercise>)

    @Insert
    suspend fun insert(routine: Routine): Long

    // Update

    @Update
    suspend fun update(exercise: Exercise)

    @Update
    suspend fun update(routine: Routine)

    @Update
    suspend fun update(routineExercise: RoutineExercise)

    // Delete

    @Delete
    suspend fun delete(exercise: Exercise)

    @Delete
    suspend fun delete(historyItem: HistoryItem)

    @Delete
    suspend fun delete(routineExercise: RoutineExercise)

    @Delete
    suspend fun delete(routine: Routine)

    // Query

    @Query("SELECT * FROM exercise_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): LiveData<List<Exercise>>

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    fun getExercise(key: Long): LiveData<Exercise?>

    @Query("""
        SELECT id, name, MAX(bpm) AS bpm FROM exercise_table
        LEFT OUTER JOIN history_table ON exerciseId = id
        GROUP BY id ORDER BY name COLLATE NOCASE ASC
        """)
    fun getAllExerciseMaxBPMs(): LiveData<List<ExerciseMaxBpm>>

    @Query("SELECT * FROM routine_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllRoutines(): LiveData<List<Routine>>

    @Query("SELECT * FROM routine_table WHERE id = :key")
    fun getRoutine(key: Long): LiveData<Routine?>

    @Query("SELECT * FROM routine_exercise_table WHERE routineId = :routineId ORDER BY `order`")
    fun getRoutineExercises(routineId: Long): List<RoutineExercise>

    @Query("""
        SELECT exerciseId, name, duration / 60 as minutes, duration % 60 AS seconds 
        FROM routine_exercise_table JOIN exercise_table ON exerciseId = exercise_table.id 
        WHERE routineId = :routineId ORDER BY `order`
        """)
    fun getRoutineExerciseNames(routineId: Long): LiveData<List<RoutineExerciseName>>

    @Query("""
        SELECT routine_exercise_table.exerciseId, name, MAX(bpm) AS bpm, duration
        FROM routine_exercise_table 
        JOIN exercise_table ON routine_exercise_table.exerciseId = exercise_table.id 
        LEFT OUTER JOIN history_table ON routine_exercise_table.exerciseId = history_table.exerciseId 
        WHERE routineId = :routineId 
        GROUP BY `order`
        ORDER BY `order`
        """)
    fun getSessionExercises(routineId: Long): LiveData<List<SessionExercise>>
}