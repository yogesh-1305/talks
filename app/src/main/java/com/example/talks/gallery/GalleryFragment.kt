package com.example.talks.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.talks.BuildConfig
import com.example.talks.databinding.FragmentGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class GalleryFragment() : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var imagesList: MutableList<String>

    companion object {
        fun newInstance(): GalleryFragment {
            return GalleryFragment()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)

        imagesList = LinkedList()
        imagesList = context?.let { listOfImages(it) }!!

        val recyclerView = binding.galleryRecyclerView
        val adapter = GalleryAdapter(imagesList, requireContext())
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = adapter

        return binding.root
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
