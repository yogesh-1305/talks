package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "talks_contacts")
data class TalksContact(
    @PrimaryKey val contactNumber: String,
    val isTalksUser: Boolean?,
    val contactName: String?,
    val contactUserName: String?,
    val contactImageUrl: String?,
    val contactImageBitmap: String?,
    val uId: String?,
    val status: String?,
    val contact_bio: String?
)