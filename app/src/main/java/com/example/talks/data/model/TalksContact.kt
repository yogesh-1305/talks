package com.example.talks.data.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "talks_contacts",
    indices = [Index(value = ["contact_number"], unique = true)])
data class TalksContact(
    @ColumnInfo(name = "contact_number")
    val contactNumber: String?,
    val contactName: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var isTalksUser: Boolean? = false
    var contactUserName: String? = null
    var contactImageUrl: String? = null
    var contactImageBitmap: Bitmap? = null
    var uId: String? = null
    var status: String? = null
    var contactBio: String? = null
}