package com.example.proyectobarreraiot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_form -> {
                    selectedFragment = FormFragment()
                }
                R.id.navigation_view_trucks -> {
                    selectedFragment = VerCamionesFragment()
                }
                R.id.navigation_settings -> {
                    selectedFragment = ConfiguracionFragment()
                }
                R.id.navigation_manual -> {
                    selectedFragment = ControlManualFragment()
                }
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }


        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navigation_form
        }
    }
}
