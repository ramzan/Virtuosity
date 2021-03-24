package ca.ramzan.virtuosity.common.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.exercises.ExerciseHistory
import ca.ramzan.virtuosity.history.SessionHistoryEntity
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineExerciseEntity
import ca.ramzan.virtuosity.session.SessionExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [
        Exercise::class,
        ExerciseHistory::class,
        Routine::class,
        RoutineExerciseEntity::class,
        SessionExercise::class,
        SessionHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExerciseDatabase : RoomDatabase() {

    abstract val exerciseDetailDao: ExerciseDetailDao
    abstract val exerciseListDao: ExerciseListDao
    abstract val historyDao: HistoryDao
    abstract val sessionDao: SessionDao
    abstract val routineEditorDao: RoutineEditorDao
    abstract val routineListDao: RoutineListDao
    abstract val summaryDao: SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExerciseDatabase::class.java,
                        "exercise_database"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                GlobalScope.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.IO) {
                                        getInstance(context).run {
                                            PREPOPULATE_EXERCISES.forEach {
                                                exerciseListDao.insert(
                                                    it
                                                )
                                            }
                                            PREPOPULATE_ROUTINES.forEach {
                                                routineEditorDao.insert(
                                                    it
                                                )
                                            }
                                            routineEditorDao.insertRoutineExercises(
                                                PREPOPULATE_ROUTINE_EXERCISES
                                            )
                                        }
                                    }
                                }
                            }
                        })
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        val PREPOPULATE_EXERCISES = listOf(
            Exercise("A Major Scale"),
            Exercise("A# Major Scale"),
            Exercise("B Major Scale"),
            Exercise("C Major Scale"),
            Exercise("C# Major Scale"),
            Exercise("D Major Scale"),
            Exercise("D# Major Scale"),
            Exercise("E Major Scale"),
            Exercise("F Major Scale"),
            Exercise("F# Major Scale"),
            Exercise("G Major Scale"),
            Exercise("G# Major Scale"),
            Exercise("A Minor Scale"),
            Exercise("A# Minor Scale"),
            Exercise("B Minor Scale"),
            Exercise("C Minor Scale"),
            Exercise("C# Minor Scale"),
            Exercise("D Minor Scale"),
            Exercise("D# Minor Scale"),
            Exercise("E Minor Scale"),
            Exercise("F Minor Scale"),
            Exercise("F# Minor Scale"),
            Exercise("G Minor Scale"),
            Exercise("G# Minor Scale"),
            Exercise("A Harmonic Minor Scale"),
            Exercise("A# Harmonic Minor Scale"),
            Exercise("B Harmonic Minor Scale"),
            Exercise("C Harmonic Minor Scale"),
            Exercise("C# Harmonic Minor Scale"),
            Exercise("D Harmonic Minor Scale"),
            Exercise("D# Harmonic Minor Scale"),
            Exercise("E Harmonic Minor Scale"),
            Exercise("F Harmonic Minor Scale"),
            Exercise("F# Harmonic Minor Scale"),
            Exercise("G Harmonic Minor Scale"),
            Exercise("G# Harmonic Minor Scale")
        )

        val PREPOPULATE_ROUTINES = listOf(
            Routine("A", 1),
            Routine("B"),
            Routine("C"),
            Routine("D"),
            Routine("E"),
            Routine("F"),
            Routine("G"),
            Routine("H"),
            Routine("I"),
            Routine("J"),
            Routine("L"),
            Routine("M"),
            Routine("N"),
            Routine("O"),
            Routine("p"),
            Routine("Q"),
            Routine("r"),
            Routine("s"),
            Routine("T"),
        )

        val PREPOPULATE_ROUTINE_EXERCISES = listOf(
            RoutineExerciseEntity(1, 1, 1, 81000),
            RoutineExerciseEntity(1, 2, 2, 72000),
            RoutineExerciseEntity(1, 3, 3, 63000),
            RoutineExerciseEntity(1, 4, 9, 54000),
            RoutineExerciseEntity(1, 5, 2, 45000)
        )
    }
}