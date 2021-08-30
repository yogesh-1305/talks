package com.example.talks.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

object Utility {

   fun URL.toBitmap(): Bitmap? {
       return try {
           BitmapFactory.decodeStream(openStream())
       }catch (e: IOException){
           null
       }
   }

}