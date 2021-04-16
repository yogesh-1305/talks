package com.example.talks.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    var readAllUserData: LiveData<List<User>>
    private val repository: UserRepository
    var readAllContacts: LiveData<List<TalksContact>>
    var readChatListItem: LiveData<List<ChatListItem>>

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllUserData = repository.readAllUserData
        readAllContacts = repository.readContacts
        readChatListItem = repository.readChatListItem
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun addContact(contact: TalksContact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addContactList(contact)
        }
    }

    fun updateUser(contact: TalksContact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(contact)
        }
    }

    fun updateUserName(userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserName(userName)
        }
    }

    fun addChatListItem(chatListItem: ChatListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addChatListItem(chatListItem)
        }
    }

    fun updateChatListItem(chatListItem: ChatListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChatListItem(chatListItem)
        }
    }

}