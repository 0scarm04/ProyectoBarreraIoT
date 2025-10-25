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

    // 1. Add a variable for Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Initialize Firebase Auth
        auth = Firebase.auth

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_form -> {
                    val formFragment = FormFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, formFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_view_trucks -> {
                    val verCamionesFragment = VerCamionesFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, verCamionesFragment).commit()
                    return@setOnItemSelectedListener true
                }
                // 3. Add the case for the logout button
                R.id.navigation_logout -> {
                    signOutAndGoToLogin()
                    return@setOnItemSelectedListener false // Don't select the logout item
                }
            }
            false
        }

        // Set the default fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navigation_form
        }
    }

    // 4. Create the function to handle signing out and navigating
    private fun signOutAndGoToLogin() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        // Flags to clear the activity stack so the user can't go back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
