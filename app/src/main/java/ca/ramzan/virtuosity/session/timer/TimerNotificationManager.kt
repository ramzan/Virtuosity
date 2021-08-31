package ca.ramzan.virtuosity.session.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*

class TimerNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManager
) {

    // region private ------------------------------------------------------------------------------

    private val timeRemainingPrefix by lazy {
        context.getString(R.string.timer_notification_time_remaining_prefix)
    }
    private val timeUpString by lazy {
        context.getString(R.string.timer_notification_time_up_message)
    }

    private val runningNotification by lazy { getTimerNotificationBuilder(TimerState.RUNNING) }
    private val pausedNotification by lazy { getTimerNotificationBuilder(TimerState.PAUSED) }
    private val stoppedNotification by lazy { getTimerNotificationBuilder(TimerState.STOPPED) }

    private var notification = stoppedNotification

    private fun getAction(
        requestCode: Int,
        intentAction: String,
        @DrawableRes icon: Int,
        title: String
    ): NotificationCompat.Action {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(intentAction),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).run {
            NotificationCompat.Action.Builder(
                icon,
                title,
                this
            ).build()
        }
    }

    private fun getResumeAction(): NotificationCompat.Action {
        return getAction(
            REQUEST_CODE_RESUME,
            RESUME_TIMER,
            R.drawable.ic_baseline_play_arrow_24,
            context.getString(R.string.start_timer)
        )
    }

    private fun getPauseAction(): NotificationCompat.Action {
        return getAction(
            REQUEST_CODE_PAUSE,
            PAUSE_TIMER,
            R.drawable.ic_baseline_pause_24,
            context.getString(R.string.pause_timer)
        )
    }

    private fun getRestartAction(): NotificationCompat.Action {
        return getAction(
            REQUEST_CODE_RESTART,
            RESTART_TIMER,
            R.drawable.ic_baseline_replay_24,
            context.getString(R.string.restart_timer)
        )
    }

    private fun getBaseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            context.getString(R.string.timer_notification_channel_id)
        )
            .setTicker(context.getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_quarter_rest_24)
            .setColor(context.resources.getColor(R.color.brown_2, null))
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText(context.getString(R.string.timer_notification_stopped_message))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    TIMER_NOTIFICATION_ID,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
    }

    private fun getTimerNotificationBuilder(timerState: TimerState): NotificationCompat.Builder {
        return getBaseNotificationBuilder().apply {
            if (timerState == TimerState.STOPPED) return this
            addAction(if (timerState == TimerState.PAUSED) getResumeAction() else getPauseAction())
            addAction(getRestartAction())
            setStyle(MediaStyle().setShowActionsInCompactView(0, 1))
        }
    }

    // endregion private ---------------------------------------------------------------------------

    // region public -------------------------------------------------------------------------------

    val serviceStartNotification: Notification get() = notification.build()

    fun updateTimerNotification(exerciseName: String, timeLeft: String, state: TimerState) {
        notification = when (state) {
            TimerState.STOPPED -> {
                notificationManager.notify(TIMER_NOTIFICATION_ID, stoppedNotification.build())
                return
            }
            TimerState.RUNNING -> runningNotification
            TimerState.PAUSED -> pausedNotification
        }
        notification.run {
            setContentTitle(exerciseName)
            setContentText("$timeRemainingPrefix $timeLeft")
            notificationManager.notify(TIMER_NOTIFICATION_ID, build())
        }
    }

    fun showTimeUpNotification(exerciseName: String) {
        notification = pausedNotification
        notification.run {
            setContentTitle(exerciseName)
            setContentText(timeUpString)
            notificationManager.notify(TIMER_NOTIFICATION_ID, build())
        }
    }

    fun updateRoutineName(name: String) {
        runningNotification.setSubText(name)
        pausedNotification.setSubText(name)
        stoppedNotification.setSubText(name)
    }
    // endregion public ----------------------------------------------------------------------------
}
