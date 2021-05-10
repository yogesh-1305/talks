package com.example.talks.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list", indices = [Index(value = ["contactNumber"], unique = true)])
data class ChatListItem(
    @ColumnInfo(name = "contactNumber")
    val contactNumber: String?,
    var latestMessage: String?,
    var unseenMessageCount: Int?,
    var timestamp: String?,
    var contactName: String?,
    var contactImageUrl: String?,
    var isChatPinned: Boolean?,
    var isChatMuted: Boolean?,
    var isChatArchived: Boolean?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}