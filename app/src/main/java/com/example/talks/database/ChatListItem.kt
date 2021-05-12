package com.example.talks.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list", indices = [Index(value = ["contactNumber"], unique = true)])
data class ChatListItem(
    @ColumnInfo(name = "contactNumber")
    var contactNumber: String?,
    var contactName: String?,
    @ColumnInfo(name = "messageText")
    var messageText: String?,
    var messageType: String?,
    var sortTimestamp: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var isChatPinned: Boolean = false
    var isChatMuted: Boolean = false
    var isChatArchived: Boolean = false
    var lastReadMessageID: Int = 0
    var unseenMessagesCount: Int = 0
}