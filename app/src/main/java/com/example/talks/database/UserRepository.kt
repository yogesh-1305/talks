package com.example.talks.database

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    val readAllData: LiveData<List<User>> = userDao.readUserData()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }
}