package com.example.talks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "talks_messages", indices = [Index(value = ["messageID"], unique = true)])
data class
Message(
    @ColumnInfo(name = "chatID") var chatId: String,
    @ColumnInfo(name = "messageID") var messageID: String?,
    var messageType: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var messageText: String? = null
    var status: String? = null
    var creationTime: String? = null
    var deliveryTime: String? = null
    var seenTime: String? = null
    var mediaSize: String? = null
    var mediaDuration: String? = null
    var mediaCaption: String? = null
    var mediaUrl: String? = null
    var mediaThumbnailString: String? = null
    var mediaLocalPath: String? = null
    var sentByMe: Boolean? = null
    var deleted: Boolean? = null
}