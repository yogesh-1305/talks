package com.example.talks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list")
data class ChatListItem(
    @ColumnInfo(name = "contactNumber")
    var contactNumber: String?,
    @ColumnInfo(name = "messageID")
    var messageID: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var isChatPinned: Boolean = false
    var isChatMuted: Boolean = false
    var isChatArchived: Boolean = false
    var lastReadMessageID: Int = 0
    var unseenMessagesCount: Int = 0
}