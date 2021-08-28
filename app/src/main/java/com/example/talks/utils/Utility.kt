package com.example.talks.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.IOException
import java.net.URL

object Utility {

    fun getBitmapFromUrl(Url: String?): Bitmap? {
        return try {
            val url = URL(Url)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            Log.d("image bitmap error", " in utility object")
            null
        }
    }

}