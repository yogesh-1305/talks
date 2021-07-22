package com.example.talks

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
            this.contact = contact
        }

        ////////////////////////////////////////////////////////////////////////
        private var image: Uri? = null

        fun imageFromGallery(image: Uri?) {
            this.image = image
        }

        fun getImage(): Uri? {
            return this.image
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