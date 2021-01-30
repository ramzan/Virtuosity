package com.nazmar.musicgym

import android.app.Activity
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.Duration



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


fun isOreoOrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun Duration.toTimerString(): String {
    return this.seconds.let { s ->
        String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60))
    }
}

fun Long.toTimerString(): String {
    return (this / 1000).let { s ->
        String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60))
    }
}