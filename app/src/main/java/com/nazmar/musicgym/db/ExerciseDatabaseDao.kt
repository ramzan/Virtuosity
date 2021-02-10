package com.nazmar.musicgym.db

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDatabaseDao {

    // Insert

    @Insert
    suspend fun insert(exercise: Exercise)

    @Insert
    suspend fun insertExercises(exercises: List<Exercise>)

    @Insert
    suspend fun insert(exerciseHistory: ExerciseHistory)

    @Insert
    suspend fun insertHistoryItems(exerciseHistories: List<ExerciseHistory>)

    @Insert
    suspend fun insert(routineExercise: RoutineExercise)

    @Insert
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExercise>)

    @Insert
    suspend fun insert(routine: Routine): Long

    @Insert
    suspend fun createSession(exercises: List<SessionExercise>)

    @Insert
    suspend fun insert(sessionHistory: SessionHistory): Long

    @Transaction
    suspend fun completeSession(
        exerciseHistories: List<SessionExercise>,
        sessionHistory: SessionHistory,
        time: Long
    ) {
        val id = insert(sessionHistory)
        insertHistoryItems(exerciseHistories.map {
            ExerciseHistory(it.exerciseId, id, it.newBpm.toInt(), time)
        })
    }

    // Update

    @Update
    suspend fun update(exercise: Exercise)

    @Update
    suspend fun update(routine: Routine)

    @Update
    suspend fun update(routineExercise: RoutineExercise)

    @Update
    suspend fun update(sessionExercise: SessionExercise)

    // Delete

    @Delete
    suspend fun delete(exercise: Exercise)

    @Delete
    suspend fun delete(exerciseExerciseHistory: ExerciseHistory)

    @Delete
    suspend fun delete(routineExercise: RoutineExercise)

    @Delete
    suspend fun delete(routine: Routine)

    @Delete
    suspend fun delete(history: SessionHistory)

    // Query

    @Query("SELECT * FROM exercise_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    fun getExercise(key: Long): Flow<Exercise?>

    @Query(
        """
        SELECT id, name, MAX(bpm) AS bpm FROM exercise_table
        LEFT OUTER JOIN exercise_history_table ON exerciseId = id
        GROUP BY id ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun getAllExerciseMaxBPMs(): Flow<List<ExerciseMaxBpm>>

    @Query("SELECT * FROM routine_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routine_table WHERE id = :key")
    suspend fun getRoutine(key: Long): Routine

    @Query("SELECT name FROM routine_table WHERE id = :key")
    suspend fun getRoutineName(key: Long): String

    @Query("SELECT * FROM routine_exercise_table WHERE routineId = :routineId ORDER BY `order`")
    fun getRoutineExercises(routineId: Long): List<RoutineExercise>

    @Query(
        """
        SELECT exerciseId, name, duration
        FROM routine_exercise_table JOIN exercise_table ON exerciseId = exercise_table.id 
        WHERE routineId = :routineId ORDER BY `order`
        """
    )
    suspend fun getRoutineExerciseNames(routineId: Long): List<RoutineExerciseName>

    @Query(
        """
        SELECT `order`, routine_exercise_table.exerciseId, name, duration, MAX(bpm) AS bpmRecord, "" AS newBpm
        FROM routine_exercise_table 
        JOIN exercise_table ON routine_exercise_table.exerciseId = exercise_table.id 
        LEFT OUTER JOIN exercise_history_table ON routine_exercise_table.exerciseId = exercise_history_table.exerciseId 
        WHERE routineId = :routineId 
        GROUP BY `order`
        ORDER BY `order`
        """
    )
    suspend fun getSessionExercises(routineId: Long): MutableList<SessionExercise>

    @Query("SELECT * FROM saved_session_table")
    suspend fun getSavedSession(): MutableList<SessionExercise>

    @Query("DELETE FROM saved_session_table")
    fun clearSavedSession()

    @Query("SELECT * FROM session_history_table WHERE id = :id")
    suspend fun getSessionHistory(id: Long): SessionHistory?

    @Query("SELECT * FROM session_history_table ORDER BY time DESC")
    fun getSessionHistories(): DataSource.Factory<Int, SessionHistory>

    @Query("SELECT EXISTS(SELECT * FROM exercise_table WHERE id = :exerciseId)")
    fun exerciseExists(exerciseId: Long): Boolean
}