package com.example.talks.data.repositories.db

import androidx.lifecycle.LiveData
import com.example.talks.data.model.*
import com.example.talks.database.dao.TalksDao
import javax.inject.Inject

class TalksRepository @Inject constructor(private val talksDao: TalksDao) {

    val readAllUserData: LiveData<List<User>> = talksDao.readUserData()
    val readContacts: LiveData<List<TalksContact>> = talksDao.readContacts()
    val readHomeScreenChannelList: LiveData<List<HomeScreenChannelList>> = talksDao.readHomeScreenChannelList()
    val readContactPhoneNumbers: LiveData<List<String>> = talksDao.readContactPhoneNumbers()
    val lastAddedMessage: LiveData<Message> = talksDao.getLastAddedMessage()
    val getChatListPhoneNumbers: LiveData<List<String>> = talksDao.getChatListPhoneNumbers()

    fun readMessages(chatID: String): LiveData<List<Message>> {
        return talksDao.readMessages(chatID)
    }

    suspend fun getLastMessageCreationTime(): String? {
        return talksDao.getLastMessageCreationTime()
    }

    suspend fun getMessagesDataForChatList() {
        talksDao.getMessagesDataForChatList()
    }

    suspend fun addUser(user: User) {
        talksDao.addUser(user)
    }

    suspend fun addContactList(contact: TalksContact) {
        talksDao.addContact(contact)
    }

    suspend fun addMessage(message: Message) {
        talksDao.addMessage(message)
    }

    suspend fun updateUser(contact: TalksContact) {
        talksDao.updateUser(contact)
    }

    suspend fun updateUserName(userName: String) {
        talksDao.updateUserName(userName)
    }

    suspend fun updateUserImage(userImage: String, imageLocalPath: String) {
        talksDao.updateUserImage(userImage, imageLocalPath)
    }

    suspend fun updateUserBio(userBio: String) {
        talksDao.updateUserBio(userBio)
    }

    suspend fun createChatChannel(chatListItem: ChatListItem) {
        talksDao.createChatChannel(chatListItem)
    }

    suspend fun createChatChannels(list: List<ChatListItem>) {
        talksDao.createChatChannels(list)
    }

    suspend fun updateMessageStatus(status: String, creationTime: String) {
        talksDao.updateMessageStatus(status, creationTime)
    }

    suspend fun updateMessageDeliveryTime(deliveryTime: String, creationTime: String) {
        talksDao.updateMessageDeliveryTime(deliveryTime, creationTime)
    }

    suspend fun updateMessageSeenTime(seenTime: String, creationTime: String) {
        talksDao.updateMessageSeenTime(seenTime, creationTime)
    }

    suspend fun updateChatListLatestMessage(
        contact_number: String,
        latestMessageId: Int
    ) {
        talksDao.updateChatListLatestMessage(contact_number, latestMessageId)
    }

    fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact> {
        return talksDao.readSingleContact(userPhoneNumber)
    }
}