package com.example.talks.database

import androidx.lifecycle.LiveData

class TalksRepository(private val talksDao: TalksDao) {

    val readAllUserData: LiveData<List<User>> = talksDao.readUserData()
    val readContacts: LiveData<List<TalksContact>> = talksDao.readContacts()
    val readChatListItem: LiveData<List<ChatListItem>> = talksDao.readChatList()
    val readContactPhoneNumbers: LiveData<List<String>> = talksDao.readContactPhoneNumbers()
    val getDistinctMessages: LiveData<List<String>> = talksDao.getDistinctMessages()
    val lastAddedMessage: LiveData<Message> = talksDao.getLastAddedMessage()
    val getChatChannels: LiveData<List<String>> = talksDao.getChatChannels()

    suspend fun readMessages(chatID: String): LiveData<List<Message>> {
        return talksDao.readMessages(chatID)
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

    suspend fun updateUserImage(userImage: String) {
        talksDao.updateUserImage(userImage)
    }

    suspend fun updateUserBio(userBio: String) {
        talksDao.updateUserBio(userBio)
    }

    suspend fun createChatChannel(chatListItem: ChatListItem) {
        talksDao.createChatChannel(chatListItem)
    }

    suspend fun updateChatChannelUserName(contactNumber: String) {
        talksDao.updateChatChannelUserName(contactNumber)
    }

    suspend fun updateChatChannel(
        messageText: String,
        sortTimestamp: String,
        messageType: String,
        contactNumber: String
    ) {
        talksDao.updateChatChannel(messageText, sortTimestamp, messageType, contactNumber)
    }

    suspend fun readSingleContact(phoneNumber: String): LiveData<TalksContact> {
        return talksDao.readSingleContact(phoneNumber)
    }
}