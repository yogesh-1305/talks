package com.example.talks.others.fileManager

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.talks.others.calendar.CalendarManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object TalksStorageManager {

    private const val ENCRYPTION_KEY = "ENCRYPTION_KEY.txt"
    private const val ENCRYPTION_KEY_PATH = "secrets/key"

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

    fun saveEncryptionKeyInInternalStorage(context: Context, file: File) {
        val dir = File(context.filesDir, ENCRYPTION_KEY_PATH)
        Log.d("dir path ===", dir.absolutePath.toString())
        if (!dir.exists()) {
            dir.mkdir()
        }

        try {
            val key = File(dir, ENCRYPTION_KEY)
            val outputStream = FileOutputStream(key)
            outputStream.flush()
            outputStream.close()
            Log.d("key path ===", key.absolutePath.toString())

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("key error ===", e.localizedMessage.toString())
        }
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