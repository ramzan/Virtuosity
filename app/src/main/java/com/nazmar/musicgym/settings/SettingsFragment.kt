package com.nazmar.musicgym.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.nazmar.musicgym.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
    }
}