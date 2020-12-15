package com.nazmar.musicgym

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
    }
}

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
    }}