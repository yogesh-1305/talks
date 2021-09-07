package com.example.talks.others

import android.net.Uri
import com.example.talks.database.TalksContact

class Helper {
    companion object {

        ////////////////////////////////////////////////////////////////////////
        private var contact: TalksContact? = null

        fun getContact(): TalksContact? {
            return contact
        }

        fun setContact(contact: TalksContact?) {
            Companion.contact = contact
        }

        ////////////////////////////////////////////////////////////////////////
        private var image: Uri? = null

        fun imageFromGallery(image: Uri?) {
            Companion.image = image
        }

        fun getImage(): Uri? {
            return image
        }

        fun setImageToNull() {
            image = null
        }

        ////////////////////////////////////////////////////////////////////////

        private var imagesList: ArrayList<Uri>? = null

        fun setImages(images: ArrayList<Uri>){
            imagesList = images
        }

        fun getImages():ArrayList<Uri>?{
            return imagesList
        }

    }


}