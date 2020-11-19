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
                                            insert(PREPOPULATE_EXERCISES)
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
    }
}