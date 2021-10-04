package com.example.talks.ui.authentication.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.talks.R
import com.example.talks.databinding.ActivityMainBinding
import com.example.talks.others.Constants.AUTH_STATE_ADD_DATA
import com.example.talks.others.Constants.AUTH_STATE_ADD_NUMBER
import com.example.talks.others.Constants.AUTH_STATE_ADD_OTP
import com.example.talks.others.Constants.AUTH_STATE_COMPLETE
import com.example.talks.others.Constants.KEY_AUTH_STATE
import com.example.talks.ui.home.activity.HomeScreenActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    @Inject
    lateinit var prefs: SharedPreferences

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

        when (prefs.getInt(KEY_AUTH_STATE, AUTH_STATE_ADD_DATA)) {

            AUTH_STATE_COMPLETE -> {
                startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
                finish()
            }
            AUTH_STATE_ADD_DATA -> {
                findNavController(R.id.fragment2).navigate(R.id.thirdFragment)
            }
            AUTH_STATE_ADD_OTP, AUTH_STATE_ADD_NUMBER -> {
                findNavController(R.id.fragment2).navigate(R.id.firstFragment)
            }
            0 -> {
                /* NO_OP */
            }
        }


    }
}