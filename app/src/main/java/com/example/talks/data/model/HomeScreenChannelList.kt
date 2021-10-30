package com.example.talks.data.model

data class HomeScreenChannelList(
    var contact_id: String = "",
    var contact_number: String = "",
    var contactName: String = "",
    var contactImageUrl: String = "",
    var messageText: String = "",
    var messageType: String = "",
    var creationTime: String = "",
    var sentByMe: Boolean? = null,
    var isChatPinned: Boolean = false,
    var isChatArchived: Boolean = false,
    var isChatMuted: Boolean = false,
    var unseenMessagesCount: Int = 0,
    var lastReadMessageID: Int? = null,
)
