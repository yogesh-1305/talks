package com.example.talks.signup.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.talks.R
import com.example.talks.home.activity.HomeScreenActivity
import com.example.talks.signup.screens.thirdFragment.ThirdFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        if (isCurrentUserLoggedIn(auth)) {
            navigateToHomeScreenActivity()
        }

//        val string = "DB5583F3E615C496FC6AA1A5BEA33"
//        val s1 = string.subSequence(1,4).toString()
//            val key = Encryption().encrypt(s1, string)
//            val key2 = Encryption().decrypt(key, string)
//            Log.i("key===", key.toString())
//            Log.i("key2===", key2.toString())
//            Log.i("string===", string)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.i("back===", "pressed")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val result = CropImage.getActivityResult(data)
            if (result!=null) {
                val imageUri = result.uri
                ThirdFragment.getImageUriFromMainActivity(imageUri)
                onResume()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("==="," ")
    }

    private fun isCurrentUserLoggedIn(auth: FirebaseAuth): Boolean {
        return auth.currentUser != null
    }

    private fun navigateToHomeScreenActivity() {
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }
}