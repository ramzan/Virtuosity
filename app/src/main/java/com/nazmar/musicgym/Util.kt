package com.nazmar.musicgym

import android.app.Activity
import android.view.View
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