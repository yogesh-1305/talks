package com.example.talks.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.talks.BuildConfig
import com.example.talks.Helper
import com.example.talks.databinding.ActivityGalleryBinding
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.chat_list_item.*
import kotlinx.coroutines.launch
import java.util.*

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var viewModel: GalleryActivityViewModel
    private lateinit var imagesList: MutableList<String>



    //encryption key (v.v.imp)
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(GalleryActivityViewModel::class.java)



        imagesList = LinkedList()
        imagesList = listOfImages(this)

        val recyclerView = binding.galleryActivityRecyclerView
        val adapter = GalleryAdapter(imagesList, this)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = adapter

        viewModel.profileImageUrl.observe(this,{

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val result = CropImage.getActivityResult(data)
            if (result != null) {
                val imageUri = result.uri.toFile()
                lifecycleScope.launch {
                    val compressedFile = Compressor.compress(this@GalleryActivity, imageUri)
                    val compressedImageUri = Uri.fromFile(compressedFile)
                    Helper.imageFromGallery(compressedImageUri)
                    finish()
                }
            }
        }
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun listOfImages(context: Context): ArrayList<String> {
        val cursor: Cursor?
        val columnIndex: Int
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
        columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

//        get folder name
//        column_index_folder = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            pathOfImage = cursor.getString(columnIndex)
            listOfAllImages.add(pathOfImage)
        }
        return listOfAllImages
    }
}