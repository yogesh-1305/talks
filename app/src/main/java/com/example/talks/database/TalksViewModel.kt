package com.example.talks.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TalksViewModel(application: Application) : AndroidViewModel(application) {

    var readAllUserData: LiveData<List<User>>
    private val repository: TalksRepository
    var readAllContacts: LiveData<List<TalksContact>>
    var readChatListItem: LiveData<List<ChatListItem>>
    var readContactPhoneNumbers: LiveData<List<String>>

    init {
        val userDao = TalksDatabase.getDatabase(application).talksDao()
        repository = TalksRepository(userDao)
        readAllUserData = repository.readAllUserData
        readAllContacts = repository.readContacts
        readChatListItem = repository.readChatListItem
        readContactPhoneNumbers = repository.readContactPhoneNumbers
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

    fun addMessage(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMessage(message)
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

    fun updateUserImage(userImage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserImage(userImage)
        }
    }

    fun updateUserBio(userBio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserBio(userBio)
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