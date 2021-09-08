package com.example.talks.fileManager

import android.app.Activity
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.example.talks.modal.InternalStoragePhoto
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.os.EnvironmentCompat
import com.example.talks.calendar.CalendarManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.appendingSink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileSystem


object TalksStorageManager {

    fun createDirectoryInPublicStorage() {

        val talksFolder = File("storage/emulated/0", "/Talks")
        talksFolder.mkdirs()

        val profilePicturesFolder = File(talksFolder, "Profile Pictures")
        profilePicturesFolder.mkdir()

        val documents = File(talksFolder, "Documents")
        documents.mkdir()

        val imagesFolder = File(talksFolder, "Images")
        imagesFolder.mkdir()

        val sentImagesFolder = File(imagesFolder, "Sent Images")
        sentImagesFolder.mkdir()

        val videosFolder = File(talksFolder, "Videos")
        videosFolder.mkdir()

        val sentVideosFolder = File(videosFolder, "Sent Videos")
        sentVideosFolder.mkdir()
    }

    fun createDirectoryInPrivateStorage(context: Context) {
        val rootFolder = File(context.getExternalFilesDir(null), "/Talks")
        rootFolder.mkdirs()

        val userImagesFolder = File(rootFolder, "/User Profiles")
        userImagesFolder.mkdir()

        val contactImagesFolder = File(rootFolder, "/Contact Images")
        contactImagesFolder.mkdir()

        val chatImagesFolder = File(rootFolder, "/Chat Images")
        chatImagesFolder.mkdir()
    }

    fun saveProfilePhotoInPrivateStorage(context: Context, bmp: Bitmap): String? {
        val rootFolder = File(context.getExternalFilesDir(null), "/Talks")
        val image = File(
            "${rootFolder.absolutePath}/User Profiles",
            "T-${CalendarManager.getCurrentDateTime()}.jpeg"
        )
        return try {
            val outputStream = FileOutputStream(image)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            image.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhotoFromPrivateStorage(context: Context, filename: String) {}
}