package com.example.talks.data.repositories.db

import androidx.lifecycle.LiveData
import com.example.talks.data.model.*
import com.example.talks.database.dao.TalksDao
import javax.inject.Inject

class TalksRepository @Inject constructor(private val talksDao: TalksDao) {

    val readAllUserData: LiveData<List<User>> = talksDao.readUserData()
    val readContacts: LiveData<List<TalksContact>> = talksDao.readContacts()
    val readChatListItem: LiveData<List<ChatListQueriedData>> = talksDao.readChatList()
    val readContactPhoneNumbers: LiveData<List<String>> = talksDao.readContactPhoneNumbers()
    val getDistinctMessages: LiveData<List<String>> = talksDao.getDistinctMessages()
    val lastAddedMessage: LiveData<Message> = talksDao.getLastAddedMessage()
    val getChatListPhoneNumbers: LiveData<List<String>> = talksDao.getChatListPhoneNumbers()
    val getDistinctPhoneNumbers: LiveData<List<String>> = talksDao.getDistinctPhoneNumbers()

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

    suspend fun updateUserImage(userImage: String, imageLocalPath: String) {
        talksDao.updateUserImage(userImage, imageLocalPath)
    }

    suspend fun updateUserBio(userBio: String) {
        talksDao.updateUserBio(userBio)
    }

    suspend fun createChatChannel(chatListItem: ChatListItem) {
        talksDao.createChatChannel(chatListItem)
    }

    suspend fun updateMessageStatus(status: String, creationTime: String) {
        talksDao.updateMessageStatus(status, creationTime)
    }


    suspend fun updateChatListLatestMessage(
        contact_number: String,
        latestMessageId: Int
    ) {
        talksDao.updateChatListLatestMessage(contact_number, latestMessageId)
    }

    suspend fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact> {
        return talksDao.readSingleContact(userPhoneNumber)
    }
}