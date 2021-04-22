package ca.ramzan.virtuosity.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ca.ramzan.virtuosity.R

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)

        findPreference<Preference>(getString(R.string.key_view_source))?.setOnPreferenceClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/ramzan/virtuosity")
                )
            )
            true
        }

        findPreference<ListPreference>(getString(R.string.key_theme))?.apply {
            setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
            setSummaryProvider { pref ->
                when (sharedPreferences.getString(
                    pref.key,
                    getString(R.string.value_theme_system)
                )) {
                    getString(R.string.value_theme_light) -> getString(R.string.display_theme_light)
                    getString(R.string.value_theme_dark) -> getString(R.string.display_theme_dark)
                    else -> getString(R.string.display_theme_system)
                }
            }
        }
    }
}