package com.example.talks.others.fileManager

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream


class FileManager {

    fun createDirectoryInExternalStorage() {

        val talksFolder = File(Environment.getExternalStorageDirectory(), "/Talks")
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

    fun saveProfileImageInExternalStorage(fragment: Fragment, image: Uri?, date: String) {

        val talksFolder = File(Environment.getExternalStorageDirectory(), "Talks")

        Glide.with(fragment).asBitmap().load(image).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (talksFolder.exists()) {
                    val profileImage =
                        File(
                            talksFolder.absolutePath + File.separator + "Profile Pictures/",
                            "IMG-$date.jpeg"
                        )
                    if (profileImage.exists()) {
                        profileImage.delete()
                    } else {
                        try {
                            val outputStream = FileOutputStream(profileImage)
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })

    }


}