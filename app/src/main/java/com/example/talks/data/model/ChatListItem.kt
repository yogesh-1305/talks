package com.example.talks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list", indices = [Index(value = ["contact_number"], unique = true)])
data class ChatListItem(
    @PrimaryKey(autoGenerate = true) val id: Int? = 0,
    @ColumnInfo(name = "contact_number")
    val contactNumber: String? = null,
    @ColumnInfo(name = "latest_message_id")
    val latestMessageId: Int?,
    val isChatPinned: Boolean? = false,
    val isChatMuted: Boolean? = false,
    val isChatArchived: Boolean? = false,
    val lastReadMessageID: Int? = 0,
    val unseenMessagesCount: Int? = 0,
)