package com.example.forceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.HashMap
import java.util.Objects

@Suppress("UNNECESSARY_SAFE_CALL", "DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val fragmentMap: MutableMap<Int, Fragment> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener)

        // Initialize fragment map
        fragmentMap[R.id.nav_opravila] = OpravilaFragmentShow()
        fragmentMap[R.id.nav_sredstva] = SredstvaFragmentShow()
        fragmentMap[R.id.nav_settings] = SettingsFragment()

        // Set the default fragment
        Objects.requireNonNull(fragmentMap[R.id.nav_opravila]).let {
            it?.let { it1 ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it1)
                    .commit()
            }
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment = fragmentMap[item.itemId]

        // Replace the fragment
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()
            return@OnNavigationItemSelectedListener true
        }
        false
    }
}