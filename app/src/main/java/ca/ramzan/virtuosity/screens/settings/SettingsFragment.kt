package ca.ramzan.virtuosity.screens.settings

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.showBottomNavBar

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            // Setting bottom padding to account for the bottom navigation view
            val tv = TypedValue()
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                this?.setPadding(
                    0,
                    0,
                    0,
                    TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)

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