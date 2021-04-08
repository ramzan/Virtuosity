package ca.ramzan.virtuosity.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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