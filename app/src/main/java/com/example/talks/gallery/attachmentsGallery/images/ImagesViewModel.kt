package com.example.talks.gallery.attachmentsGallery.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.talks.BuildConfig
import java.util.ArrayList

class ImagesViewModel : ViewModel() {

    val imagesList: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun listOfImages(context: Context){
        val cursor: Cursor?
        //        int column_index_folder;
        val listOfAllImages = ArrayList<String>()
        var pathOfImage: String
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val orderBy = MediaStore.Video.Media.DATE_TAKEN
        cursor = context.contentResolver.query(uri, projection, null, null, "$orderBy DESC")
        if (BuildConfig.DEBUG && cursor == null) {
            error("Assertion failed")
        }
        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

//        get folder name
//        column_index_folder = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            pathOfImage = cursor.getString(columnIndex)
            listOfAllImages.add(pathOfImage)
        }
        cursor.close()
        imagesList.value = listOfAllImages
    }

}