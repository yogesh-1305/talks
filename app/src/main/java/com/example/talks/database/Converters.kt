package com.example.talks.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(img: Bitmap?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        img?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        return null
    }

}