package com.nazmar.musicgym

import android.app.Activity
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomnavigation.BottomNavigationView

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