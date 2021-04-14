package ca.ramzan.virtuosity.common

import android.app.Activity
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import ca.ramzan.virtuosity.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun Activity.hideBottomNavBar() {
    this.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
}

fun Activity.showBottomNavBar() {
    this.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
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

fun millisToTimerString(millis: Long): String {
    return (millis / 1000).let { s ->
        if (s < 3600) String.format("%02d:%02d", (s % 3600) / 60, (s % 60))
        else String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60))
    }
}

fun NavController.safeNavigate(directions: NavDirections) {
    currentDestination?.getAction(directions.actionId)?.run {
        navigate(directions)
    }
}

object DateFormatter {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, MMMM d y, h:mm a")

    fun fromMilli(time: Long): String = fromInstant(Instant.ofEpochMilli(time))

    fun fromInstant(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(formatter)
}