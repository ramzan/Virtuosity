package ca.ramzan.virtuosity.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ca.ramzan.virtuosity.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val theme = prefs.getString(
            getString(R.string.key_theme),
            getString(R.string.value_theme_system)
        )

        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                getString(R.string.value_theme_light) -> AppCompatDelegate.MODE_NIGHT_NO
                getString(R.string.value_theme_dark) -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        createChannel(
            getString(R.string.timer_notification_channel_id),
            getString(R.string.timer_notification_channel_name)
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (isOreoOrAbove()) {
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