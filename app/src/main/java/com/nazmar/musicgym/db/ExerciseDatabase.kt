package com.nazmar.musicgym.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
        entities = [Exercise::class, HistoryItem::class, Routine::class, RoutineExercise::class],
        version = 1,
        exportSchema = false
)
abstract class ExerciseDatabase : RoomDatabase() {

    abstract val exerciseDatabaseDao: ExerciseDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase {
            synchronized(this) {

                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE

                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            ExerciseDatabase::class.java,
                            "exercise_database"
                    )
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this lesson. You can learn more about
                            // migration with Room in this blog post:
                            // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                            .fallbackToDestructiveMigration()
                            // prepopulate the database after onCreate was called
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    // moving to a new thread
                                    GlobalScope.launch(Dispatchers.IO) {
                                        withContext(Dispatchers.IO) {
                                            getInstance(context).exerciseDatabaseDao.apply {
                                                insertExercises(PREPOPULATE_EXERCISES)
                                                insertHistoryItems(PREPOPULATE_HISTORY)
                                                PREPOPULATE_ROUTINES.forEach { insert(it) }
                                                PREPOPULATE_ROUTINE_EXERCISES.forEach { insert(it) }
                                            }
                                        }
                                    }
                                }
                            })
                            .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }

                // Return instance; smart cast to be non-null.
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
                Exercise("G# Major Scale")
        )

        val PREPOPULATE_HISTORY = listOf(
                HistoryItem(1, 5),
                HistoryItem(1, 30),
                HistoryItem(1, 1),
                HistoryItem(3, 5),
                HistoryItem(4, 5),
                HistoryItem(5, 5),
                HistoryItem(6, 5),
                HistoryItem(7, 5),
                HistoryItem(8, 5),
                HistoryItem(9, 5),
                HistoryItem(10, 5),
                HistoryItem(12, 50),
        )

        val PREPOPULATE_ROUTINES = listOf(
                Routine(1, "A"),
                Routine("B"),
                Routine("C"),
                Routine("D"),
                Routine("E"),
        )

        val PREPOPULATE_ROUTINE_EXERCISES = listOf(
                RoutineExercise(1, 1, 1, 81),
                RoutineExercise(1, 2, 2, 72),
                RoutineExercise(1, 3, 3, 63),
                RoutineExercise(1, 4, 10, 54),
                RoutineExercise(1, 5, 2, 45)
                )
    }
}