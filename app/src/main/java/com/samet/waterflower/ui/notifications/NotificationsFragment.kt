package com.samet.waterflower.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.samet.waterflower.databinding.FragmentNotificationsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Anahtar kayıtlı mı?
        val isDarkSaved = prefs.getBoolean(KEY_DARK_MODE, false)
        binding.switchTheme.isChecked = isDarkSaved

        // Tema değiştirici
        binding.switchTheme.setOnCheckedChangeListener { _: CompoundButton, isChecked ->
            val mode = if (isChecked)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            AppCompatDelegate.setDefaultNightMode(mode)
            prefs.edit()
                .putBoolean(KEY_DARK_MODE, isChecked)
                .apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
