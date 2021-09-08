package com.example.talks.others.utility

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.talks.home.activity.HomeScreenActivity
import android.content.Context

object ConversionUtility {

    fun Uri.toBitmap(activity: Activity): Bitmap {
        val inputStream = activity.contentResolver.openInputStream(this)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmap
    }

    suspend fun String.toBitmap(context: Context): Bitmap {
        val loading = coil.ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(this)
            .build()
        return ((loading.execute(request) as SuccessResult).drawable as BitmapDrawable).bitmap
    }

}