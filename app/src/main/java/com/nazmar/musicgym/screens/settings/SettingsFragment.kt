package com.nazmar.musicgym.screens.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.showBottomNavBar

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
    }
}