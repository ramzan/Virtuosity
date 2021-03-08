package ca.ramzan.virtuosity.screens.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.showBottomNavBar

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
    }
}