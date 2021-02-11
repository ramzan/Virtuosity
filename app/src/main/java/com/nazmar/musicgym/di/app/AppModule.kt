package com.nazmar.musicgym.di.app

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.nazmar.musicgym.db.ExerciseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun dao(application: Application) =
        ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    @Provides
    @Singleton
    fun prefs(application: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)

}