package com.example.talks.database

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readAllUserData: LiveData<List<User>> = userDao.readUserData()
    val readContacts: LiveData<List<TalksContact>> = userDao.readContacts()
    val readChatListItem: LiveData<List<ChatListItem>> = userDao.readChatList()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }

    suspend fun addContactList(contact: TalksContact){
        userDao.addContact(contact)
    }

    suspend fun updateUser(contact: TalksContact){
        userDao.updateUser(contact)
    }

    suspend fun updateUserName(userName: String){
        userDao.updateUserName(userName)
    }

    suspend fun addChatListItem(chatListItem: ChatListItem){
        userDao.addChatListItem(chatListItem)
    }

    suspend fun updateChatListItem(chatListItem: ChatListItem){
        userDao.updateChatListItem(chatListItem)
    }
}