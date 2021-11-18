package com.example.talks.data.model

import androidx.room.ColumnInfo

data class ChatListQueriedData(
    @ColumnInfo(name = "chatID")
    val chatID: String? = "",
    @ColumnInfo(name = "latest_message_id")
    val latest_message_id: Int? = null
) {
    companion object {

        fun ChatListQueriedData.toChatListItem(): ChatListItem {
            return ChatListItem(
                contactNumber = chatID,
                latestMessageId = latest_message_id
            )
        }

    }
}