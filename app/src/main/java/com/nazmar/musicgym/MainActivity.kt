package com.nazmar.musicgym

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nazmar.musicgym.data.Repository
import com.nazmar.musicgym.db.ExerciseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Repository.apply {
            setDataSource(ExerciseDatabase.getInstance(application).exerciseDatabaseDao)
            setPreferences(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        }

        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        createChannel(
            getString(R.string.timer_notification_channel_id),
            getString(R.string.timer_notification_channel_name)
        )
        createChannel(
            getString(R.string.metronome_notification_channel_id),
            getString(R.string.metronome_notification_channel_name)
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (isOreoOrAbove()) {
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = channelName
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }
}