package com.nazmar.musicgym.db

import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDatabaseDao {

    // region Exercise list
    @Insert
    suspend fun insert(exercise: Exercise)

    @Query(
        """
        SELECT id, name, MAX(bpm) AS bpm FROM exercise_table
        LEFT OUTER JOIN exercise_history_table ON exerciseId = id
        GROUP BY id ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun getAllExerciseMaxBPMs(): Flow<List<ExerciseMaxBpm>>
    // endregion Exercise list

    // region Routine editor
    @Insert
    suspend fun insert(routine: Routine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExercise>)

    @Update
    suspend fun update(routine: Routine)

    @Delete
    suspend fun delete(routineExercises: List<RoutineExercise>)

    @Delete
    suspend fun delete(routine: Routine)

    @Query("SELECT * FROM routine_table WHERE id = :key")
    suspend fun getRoutine(key: Long): Routine

    @Query("SELECT * FROM exercise_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<Exercise>>

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

    @Transaction
    suspend fun createRoutine(routineName: String, exercises: List<RoutineExerciseName>) {
        val newRoutineId = insert(Routine(routineName))
        var order = 1
        insertRoutineExercises(exercises.map {
            RoutineExercise(newRoutineId, order++, it.exerciseId, it.duration)
        })
    }

    @Transaction
    suspend fun updateRoutine(
        routine: Routine,
        updatedExercises: List<RoutineExercise>,
        deletedExercises: List<RoutineExercise>
    ) {
        update(routine)
        insertRoutineExercises(updatedExercises)
        delete(deletedExercises)
    }
    // endregion Routine editor

    // region Session
    @Insert
    suspend fun insertHistoryItems(exerciseHistories: List<ExerciseHistory>)

    @Insert
    suspend fun createSession(exercises: List<SessionExercise>)

    @Insert
    suspend fun insert(sessionHistory: SessionHistory): Long

    @Update
    suspend fun update(sessionExercise: SessionExercise)

    @Query("DELETE FROM saved_session_table")
    fun clearSavedSession()

    @Query("SELECT name FROM routine_table WHERE id = :key")
    suspend fun getRoutineName(key: Long): String

    @Query("SELECT * FROM saved_session_table")
    suspend fun getSavedSession(): MutableList<SessionExercise>

    @Query("SELECT EXISTS(SELECT * FROM exercise_table WHERE id = :exerciseId)")
    fun exerciseExists(exerciseId: Long): Boolean

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
    // endregion Session

    // region Exercise Detail
    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    fun getExercise(key: Long): Flow<Exercise?>
    // endregion Exercise Detail

    // region History
    @Delete
    suspend fun delete(history: SessionHistory)

    @Query("SELECT * FROM session_history_table WHERE id = :id")
    suspend fun getSessionHistory(id: Long): SessionHistory?

    @Query("SELECT * FROM session_history_table ORDER BY time DESC")
    fun getSessionHistories(): DataSource.Factory<Int, SessionHistory>
    // endregion History

    // region Routine list
    @Query("SELECT * FROM routine_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllRoutines(): Flow<List<Routine>>
    // endregion Routine list
}