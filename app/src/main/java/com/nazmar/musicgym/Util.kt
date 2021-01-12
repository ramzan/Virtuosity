package com.nazmar.musicgym

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nazmar.musicgym.practice.session.TimerService

enum class TimerState {
    STOPPED, // Timer has not been created
    RUNNING, // Timer is counting down
    PAUSED, // Pause button pressed, time still remaining
    COMPLETED // Timer at 0 and alarm has been rung
}

fun Activity.hideBottomNavBar() {
    this.findViewById<BottomNavigationView>(R.id.nav_view)?.let {
        it.visibility = View.GONE
    }
}

fun Activity.showBottomNavBar() {
    this.findViewById<BottomNavigationView>(R.id.nav_view)?.let {
        it.visibility = View.VISIBLE
    }
}

fun InputMethodManager.hideKeyboard(windowToken: IBinder) {
    this.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
    )
}

fun InputMethodManager.showKeyboard() {
    this.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_NOT_ALWAYS
    )
}

const val TIMER_NOTIFICATION_ID = 0

fun getTimerNotificationBuilder(
        context: TimerService,
        playAction: NotificationCompat.Action,
        restartAction: NotificationCompat.Action
): NotificationCompat.Builder {

    val contentPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            TIMER_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    return NotificationCompat.Builder(context, context.application.getString(R.string.timer_notification_channel_id))
            .setTicker(context.application.getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .addAction(playAction)
            .addAction(restartAction)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
}