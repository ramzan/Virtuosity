package com.nazmar.musicgym

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nazmar.musicgym.practice.session.*

enum class TimerState {
    STOPPED, // Timer has not been created
    RUNNING, // Timer is counting down
    PAUSED, // Pause button pressed, time still remaining
}

const val MAX_TIMER_DURATION = 5999000L
const val DEFAULT_TIMER_DURATION = 300000L

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

fun Activity.getInputMethodManager(): InputMethodManager {
    return this.getSystemService(InputMethodManager::class.java)
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

const val TIMER_NOTIFICATION_ID = 1

const val REQUEST_CODE_RESTART = 0
const val REQUEST_CODE_PAUSE = 1
const val REQUEST_CODE_RESUME = 2


fun getTimerNotificationBuilder(
    context: TimerService,
    timerState: TimerState
): NotificationCompat.Builder {

    val contentPendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        TIMER_NOTIFICATION_ID,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        context,
        context.application.getString(R.string.timer_notification_channel_id)
    )
        .setTicker(context.application.getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_baseline_music_note_24)
        .setOngoing(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(contentPendingIntent)
        .setContentText("Practice in session")

    if (timerState != TimerState.STOPPED) {

        val playAction = if (timerState == TimerState.PAUSED) {
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_RESUME,
                Intent(RESUME_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
            ).let { resumePendingIntent ->
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_play_arrow_24,
                    context.getString(R.string.start_timer),
                    resumePendingIntent
                ).build()
            }
        } else {
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PAUSE,
                Intent(PAUSE_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
            ).let { pausePendingIntent ->
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_pause_24,
                    context.getString(R.string.pause_timer),
                    pausePendingIntent
                ).build()
            }
        }

        val restartAction = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESTART,
            Intent(RESTART_TIMER),
            PendingIntent.FLAG_UPDATE_CURRENT
        ).let { restartPendingIntent ->
            NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_replay_24,
                context.getString(R.string.restart_timer),
                restartPendingIntent
            ).build()
        }

        builder.apply {
            addAction(playAction)
            addAction(restartAction)
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
        }
    }
    return builder
}

fun isOreoOrAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

fun MutableLiveData<MutableList<SummaryExercise>>.updateBpm(index: Int, newBpm: String) {
    val value = this.value?.toMutableList() ?: mutableListOf()
    val bpm = if (newBpm.isBlank()) {
        0
    } else {
        newBpm.toInt()
    }
    value[index] = SummaryExercise(value[index].id, value[index].name, value[index].oldBpm, bpm)
    this.value = value

}