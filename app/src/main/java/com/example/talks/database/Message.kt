package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "talks_messages")
class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val messageText: String,
    val messageStatus: String,
    val needsPush: Boolean,
    val send_timestamp: String,
    val received_timestamp: String,
    val sender_id: String,
    val receiver_id: String,
)