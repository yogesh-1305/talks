package com.example.talks.home.profileScreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.talks.R
import com.example.talks.databinding.ActivityProfileSettingsBinding

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}