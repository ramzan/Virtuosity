package ca.ramzan.virtuosity.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.hideBottomNavBar
import ca.ramzan.virtuosity.databinding.FragmentSettingsBinding
import ca.ramzan.virtuosity.screens.BaseFragment

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        requireActivity().hideBottomNavBar()

        setUpBinding(FragmentSettingsBinding.inflate(inflater))

        childFragmentManager
            .beginTransaction()
            .replace(R.id.preferences_container, PreferencesFragment())
            .commit()

        binding.settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }
}