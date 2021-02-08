package com.example.talks.signup.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.talks.R
import com.example.talks.gallery.GalleryFragment
import com.example.talks.home.HomeScreenActivity
import com.example.talks.signup.screens.thirdFragment.ThirdFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage

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
        if(data != null){
            val result = CropImage.getActivityResult(data)
            val imageUri = result.uri
            ThirdFragment.getImageUriFromMainActivity(imageUri)
            val frag = GalleryFragment.newInstance()
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