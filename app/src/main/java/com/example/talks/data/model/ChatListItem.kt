package com.example.talks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list", indices = [Index(value = ["contact_number"], unique = true)])
data class ChatListItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "contact_number") var contactNumber: String?,
    @ColumnInfo(name = "latest_message_id") var latestMessageId: Int?,
    var isChatPinned: Boolean = false,
    var isChatMuted: Boolean = false,
    var isChatArchived: Boolean = false,
    var lastReadMessageID: Int = 0,
    var unseenMessagesCount: Int = 0,
)