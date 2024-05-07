package com.example.forceapp


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Initialize SharedPrefManager
        sharedPrefManager = SharedPrefManager(requireContext())

        // Logout preference
        val logoutPreference: Preference? = findPreference("logout_preference")
        logoutPreference?.setOnPreferenceClickListener {
            // Clear shared preferences
            sharedPrefManager.logout()

            Intent(requireContext(), StartupActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Dark mode preference
        val darkModePreference: SwitchPreferenceCompat? = findPreference("night")
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Check the value of "night" in SharedPreferences and set the switch accordingly
        val nightModeEnabled = sharedPreferences.getBoolean("night", false)
        darkModePreference?.isChecked = nightModeEnabled

        darkModePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val nightModeEnabled = newValue as Boolean

                if (nightModeEnabled) {
                    // Set the dark mode for the app
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    // Set the dark mode for the app
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                // Save the new value in SharedPreferences
                editor.putBoolean("night", nightModeEnabled)
                editor.apply()

                // Return to MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                true
            }

    }
}


