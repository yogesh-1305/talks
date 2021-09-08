package com.example.talks.signup.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.talks.databinding.ActivityMainBinding
import com.example.talks.home.activity.HomeScreenActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToHomeScreenActivity()
        }
    }

    private fun navigateToHomeScreenActivity() {
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }
}