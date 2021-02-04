package com.example.talks.gallery

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talks.databinding.GalleryItemBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class GalleryAdapter(private val images: MutableList<String>, private val context: Context) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {
    class GalleryViewHolder(val binding : GalleryItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(GalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val view = holder.binding
        val image = images[position]
        var imageUri : Uri
        Glide.with(context).load(image).into(view.galleryImage)

        view.galleryImage.setOnClickListener{
            Toast.makeText(context, image, Toast.LENGTH_LONG).show()
            imageUri = Uri.fromFile(File(image))
            UCrop.of(imageUri, Uri.fromFile(File(image+"cropped.jpg")))
                .withAspectRatio(1F, 1F)
                .start(context as Activity)
        }

    }

    override fun getItemCount(): Int {
        Log.i("Images size===", images.size.toString())
       return images.size
    }
}