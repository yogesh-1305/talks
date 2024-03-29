package com.example.talks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "talks_messages", indices = [Index(value = ["creation_time"], unique = true)])
data class
    Message(
        @PrimaryKey(autoGenerate = true)    val id: Int = 0,
        @ColumnInfo(name = "chatID")        val chatID: String? = null,
        @ColumnInfo(name = "senderID")      val senderID: String? = null,
        @ColumnInfo(name = "creation_time") val creationTime: String? = null,
                                            val messageType: String? = null,
                                            val messageText: String? = null,
                                            val status: String? = null,
                                            val deliveryTime: String? = null,
                                            val seenTime: String? = null,
                                            val mediaSize: String? = null,
                                            val mediaDuration: String? = null,
                                            val mediaCaption: String? = null,
                                            val mediaUrl: String? = null,
                                            val mediaThumbnailString: String? = null,
                                            val mediaLocalPath: String? = null
) {
    constructor() : this(
        0, "", "", "", "",
        "", "", "", "",
        "", "", "", "", "", null
    )

    companion object {
        fun Message.toTextMessage(): TextMessage {
            return TextMessage(
                chatID = chatID,
                senderID = senderID,
                messageType = messageType,
                messageText = messageText,
                creationTime = creationTime,
                deliveryTime = deliveryTime,
                seenTime = seenTime,
            )
        }
    }
}

data class TextMessage(
    val chatID: String? = null,
    val senderID: String? = null,
    val messageType: String?,
    val messageText: String? = null,
    val creationTime: String? = null,
    val deliveryTime: String? = null,
    val seenTime: String? = null
)