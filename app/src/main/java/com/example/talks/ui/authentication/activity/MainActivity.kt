package com.example.talks.ui.authentication.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.talks.R
import com.example.talks.databinding.ActivityMainBinding
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_DATA
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_NUMBER
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_OTP
import com.example.talks.constants.LocalConstants.AUTH_STATE_COMPLETE
import com.example.talks.constants.LocalConstants.KEY_AUTH_STATE
import com.example.talks.ui.home.activity.HomeScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    private var screenState: ScreenState = ScreenState.AT_WELCOME_SCREEN

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavController()
        verifyStateAndNavigateToHomeScreen()

    }

    private fun setupNavController() {
        navController = findNavController(R.id.auth_activity_nav_host)
        destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.welcomeFragment -> {
                        screenState = ScreenState.AT_WELCOME_SCREEN
                    }
                    R.id.firstFragment -> {
                        screenState = ScreenState.AT_PHONE_SCREEN
                    }
                    R.id.secondFragment -> {
                        screenState = ScreenState.AT_VERIFICATION_SCREEN
                    }
                    R.id.thirdFragment -> {
                        screenState = ScreenState.AT_DATA_SCREEN
                    }
                }
            }
    }

    private fun verifyStateAndNavigateToHomeScreen() {

        when (prefs.getInt(KEY_AUTH_STATE, AUTH_STATE_ADD_DATA)) {

            AUTH_STATE_COMPLETE -> {
                startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
                finish()
            }
            AUTH_STATE_ADD_DATA -> {
                findNavController(R.id.auth_activity_nav_host).navigate(R.id.thirdFragment)
            }
            AUTH_STATE_ADD_OTP, AUTH_STATE_ADD_NUMBER -> {
                findNavController(R.id.auth_activity_nav_host).navigate(R.id.firstFragment)
            }
            0 -> {
                /* NO_OP */
            }
        }


    }

    override fun onResume() {
        navController.addOnDestinationChangedListener(destinationChangedListener)
        super.onResume()
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }

    override fun onBackPressed() {
        when (screenState) {
            ScreenState.AT_WELCOME_SCREEN,
            ScreenState.AT_PHONE_SCREEN -> finish()

            ScreenState.AT_VERIFICATION_SCREEN,
            ScreenState.AT_DATA_SCREEN -> moveTaskToBack(true)
        }

    }
}

enum class ScreenState {
    AT_WELCOME_SCREEN,
    AT_PHONE_SCREEN,
    AT_VERIFICATION_SCREEN,
    AT_DATA_SCREEN,

}