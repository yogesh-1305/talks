package com.example.talks.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "talks_messages", indices = [Index(value = ["messageID"], unique = true)])
data class
Message(
    var chatId: String,
    @ColumnInfo(name = "messageID") val messageID: String?,
    var messageText: String?,
    var messageStatus: String?,
    var needsPush: Boolean?,
    var sendTime: String?,
    var sendDate: String?,
    var sentByMe: Boolean?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}