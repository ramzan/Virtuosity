package ca.ramzan.virtuosity.session.timer

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.TIMER_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject


const val RESUME_TIMER = "resume_timer"
const val PAUSE_TIMER = "pause_timer"
const val RESTART_TIMER = "restart_timer"

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var timer: Timer

    private lateinit var timerReceiver: TimerReceiver

    private lateinit var timerNotificationManager: TimerNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(context = Dispatchers.Main + serviceJob)


    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer.create(this, R.raw.bell)

        val vibrator = prefs.getBoolean(getString(R.string.key_timer_vibrate), true).let {
            if (it) getSystemService(Vibrator::class.java) else null
        }

        timerNotificationManager = TimerNotificationManager(this, notificationManager)

        timer = Timer(
            timerNotificationManager,
            mediaPlayer,
            vibrator,
            serviceScope
        )
        timerReceiver = TimerReceiver(timer)
        IntentFilter().apply {
            addAction(RESUME_TIMER)
            addAction(PAUSE_TIMER)
            addAction(RESTART_TIMER)
        }.also {
            registerReceiver(timerReceiver, it)
        }
        binder = TimerBinder(timerNotificationManager, timer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(TIMER_NOTIFICATION_ID, timerNotificationManager.serviceStartNotification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timerReceiver)
        timer.stopTimer()
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
        serviceJob.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder = binder

    private lateinit var binder: TimerBinder
}

class TimerBinder(
    private val notificationManager: TimerNotificationManager,
    private val timer: Timer
) : Binder() {
    fun getTimer(): Timer = timer
    fun updateRoutineName(name: String) = notificationManager.updateRoutineName(name)
}