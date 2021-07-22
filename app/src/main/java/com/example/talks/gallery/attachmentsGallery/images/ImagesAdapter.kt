package com.example.talks.gallery.attachmentsGallery.images

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talks.R
import com.example.talks.databinding.GalleryItemBinding
import com.example.talks.gallery.attachmentsGallery.AttachmentStateListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

class ImagesAdapter(private val images: MutableList<String>, private val context: Context) :
    RecyclerView.Adapter<ImagesAdapter.GalleryViewHolder>() {
    class GalleryViewHolder(val binding: GalleryItemBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var listener: AttachmentStateListener
    fun setAttachmentStateListener(callback: AttachmentStateListener){
        listener = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(
            GalleryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private val listOfImagesToBeSelected = ArrayList<Uri>()
    private val listOfGalleryViews = ArrayList<GalleryItemBinding>()
    var isMultiSelectStateEnabled = false
    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val view = holder.binding
        val image = images[position]
        var imageUri: Uri
        Glide.with(context).load(image).into(view.galleryImage)
        listOfGalleryViews.add(view)

        view.galleryImage.setOnLongClickListener {
            imageUri = Uri.fromFile(File(image))
            if (!isMultiSelectStateEnabled) {
                isMultiSelectStateEnabled = true
                listOfImagesToBeSelected.add(imageUri)
                enableSelectedState(view)
                listener.state(true, listOfImagesToBeSelected.size)
            }
            return@setOnLongClickListener true
        }

        view.galleryImage.setOnClickListener {
            imageUri = Uri.fromFile(File(image))
            if (isMultiSelectStateEnabled) {
                if (listOfImagesToBeSelected.contains(imageUri)) {
                    listOfImagesToBeSelected.remove(imageUri)
                    disableSelectedState(view)
                    listener.state(true, listOfImagesToBeSelected.size)
                } else {
                    enableSelectedState(view)
                    listOfImagesToBeSelected.add(imageUri)
                    listener.state(true, listOfImagesToBeSelected.size)
                }
                if (listOfImagesToBeSelected.isNullOrEmpty()) {
                    isMultiSelectStateEnabled = false
                    listener.state(false, listOfImagesToBeSelected.size)
                }
            }
        }

    }

    fun clearSelection(){
        for (data in listOfGalleryViews){
            disableSelectedState(data)
        }
        listOfImagesToBeSelected.clear()
        isMultiSelectStateEnabled = false
        listener.state(false, listOfImagesToBeSelected.size)
    }

    fun getSelectedImagesList(): ArrayList<Uri>{
        return listOfImagesToBeSelected
    }

    private fun enableSelectedState(view: GalleryItemBinding) {
        view.galleryImage.foreground = ColorDrawable(Color.parseColor("#66000000"))
        view.imageSelectedTickMark.visibility = View.VISIBLE
    }

    private fun disableSelectedState(view: GalleryItemBinding) {
        view.imageSelectedTickMark.visibility = View.INVISIBLE
        view.galleryImage.foreground = null
    }

    override fun getItemCount(): Int {
        return images.size
    }
}