package com.example.talks.data.model

class ChatListQueriedData(
    var chatID: String? = "",
    var latest_message_id: Int? = null,
    var messageText: String? = "",
    var messageType: String? = "",
    var status: String? = "",
    var sentByMe: Boolean? = null,
)