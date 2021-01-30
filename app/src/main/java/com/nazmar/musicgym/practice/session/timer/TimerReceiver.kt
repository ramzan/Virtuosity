package com.nazmar.musicgym.practice.session.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerReceiver(private val timer: Timer) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            RESUME_TIMER -> timer.startTimer()
            PAUSE_TIMER -> timer.pauseTimer()
            RESTART_TIMER -> timer.restartTimer()
        }
    }
}