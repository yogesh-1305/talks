package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list")
data class ChatListItem (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val image: String,
    val latestMessage: String,
    val isActive: Boolean,
    val hasUnreadMessages: Boolean,
    val timestamp: String
)