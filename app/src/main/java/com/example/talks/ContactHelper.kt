package com.example.talks

import android.graphics.Bitmap
import com.example.talks.database.TalksContact

class ContactHelper {
    companion object {
        private var contact: TalksContact? = null

        fun getContact(): TalksContact?{
            return contact
        }

        fun setContact(contact: TalksContact?){
            this.contact = contact
        }
    }


}