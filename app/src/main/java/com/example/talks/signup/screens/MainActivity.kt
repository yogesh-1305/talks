package com.example.talks.signup.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.talks.R
import com.example.talks.home.HomeScreenActivity
import com.example.talks.signup.screens.thirdFragment.ThirdFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        if (isCurrentUserLoggedIn(auth)) {
            navigateToHomeScreenActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            val resultUri = UCrop.getOutput(data!!)
            ThirdFragment.getImageUriFromMainActivity(resultUri)


        } else if (resultCode == UCrop.RESULT_ERROR) {

            val cropError = UCrop.getError(data!!)
            ThirdFragment.getCropImageErrorCode(cropError)
        }
    }

    private fun isCurrentUserLoggedIn(auth : FirebaseAuth): Boolean{
        return auth.currentUser != null
    }

    private fun navigateToHomeScreenActivity(){
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }
}