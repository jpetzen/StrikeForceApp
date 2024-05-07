package com.example.forceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate


@Suppress("DEPRECATION")
class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check the saved theme preference and apply the correct theme
        val sharedPreferences: SharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightModeEnabled = sharedPreferences.getBoolean("night", false)

        if (nightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)

        // Check if internet is available
        if (!isNetworkAvailable()) {
            showNoInternetDialog()
        } else {
            // Proceed with your app initialization
            setContentView(R.layout.activity_startup)

            // Initialize buttons and set click listeners
            val btnLogin = findViewById<Button>(R.id.btn_sign)
            val btnRegister = findViewById<Button>(R.id.btn_register)

            btnLogin.setOnClickListener {
                val intent = Intent(this@StartupActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            btnRegister.setOnClickListener {
                val intent = Intent(this@StartupActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please turn on your internet connection to use this app")
        builder.setPositiveButton("Settings") { _: DialogInterface, _: Int ->
            // Open settings to enable internet
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        builder.setNegativeButton("Exit") { _: DialogInterface, _: Int ->
            // Close the app
            finish()
        }
        builder.setCancelable(false) // Prevent dismissing dialog on outside touch
        builder.show()
    }
}