package com.example.talks.database

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readAllUserData: LiveData<List<User>> = userDao.readUserData()
    val readContacts: LiveData<List<TalksContact>> = userDao.readContacts()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }

    suspend fun addContactList(contact: TalksContact){
        userDao.addContact(contact)
    }

    suspend fun updateUser(contact: TalksContact){
        userDao.updateUser(contact)
    }
}