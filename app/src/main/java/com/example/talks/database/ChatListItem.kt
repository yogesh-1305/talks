package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list")
data class ChatListItem (
    @PrimaryKey val contactNumber: String,
    val latestMessage: String?,
    val unseenMessageCount: Int?,
    val timestamp: String?,
    val contactName: String?,
    val contactUserName: String?,
    val contactImageUrl: String?,
    val contactImageBitmap: String?,
    val uId: String?,
    val status: String?,
    val contact_bio: String?
)