package com.nazmar.musicgym

import android.app.Activity
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Activity.hideBottomNavBar() {
    val bar = this.findViewById<BottomNavigationView>(R.id.nav_view)
    if (bar != null) {
        bar.visibility = View.GONE
    }
}

fun Activity.showBottomNavBar() {
    val bar = this.findViewById<BottomNavigationView>(R.id.nav_view)
    if (bar != null) {
        bar.visibility = View.VISIBLE
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