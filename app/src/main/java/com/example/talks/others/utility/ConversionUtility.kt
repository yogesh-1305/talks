package com.example.talks.others.utility

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.request.ImageRequest
import android.content.Context
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.request.SuccessResult
import java.io.IOException

object ConversionUtility {

    fun Uri.toBitmap(activity: Activity): Bitmap? {
        return try {
            val inputStream = activity.contentResolver.openInputStream(this)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }

    suspend fun String.toBitmap(context: Context): Bitmap? {
        return try {
            val loading = ImageLoader(context)
            val request: ImageRequest = ImageRequest.Builder(context)
                .data(this)
                .build()

            val result: SuccessResult = loading.execute(request) as SuccessResult
            val bitmap = result.drawable
            (bitmap as BitmapDrawable).bitmap

        } catch (e: Exception) {
            null
        }
    }

}