package com.example.todolistxml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistxml.data.ui.fragments.Habits.HabitsFragment
import com.example.todolistxml.data.ui.fragments.Home.HomeFragment
import com.example.todolistxml.data.ui.fragments.Profile.ProfileFragment
import com.example.todolistxml.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_habits -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HabitsFragment())
                        .commit()
                    true
                }

                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
}