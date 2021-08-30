package com.example.talks.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TalksViewModel @Inject constructor(private val repository: TalksRepository) : ViewModel() {

    var readAllUserData: LiveData<List<User>> = repository.readAllUserData
    var readAllContacts: LiveData<List<TalksContact>> = repository.readContacts
    var readChatListItem: LiveData<List<ChatListQueriedData>> = repository.readChatListItem
    var readContactPhoneNumbers: LiveData<List<String>> = repository.readContactPhoneNumbers
    var getDistinctMessages: LiveData<List<String>> = repository.getDistinctMessages
    var lastAddedMessage: LiveData<Message> = repository.lastAddedMessage
    var getChatListPhoneNumbers: LiveData<List<String>> = repository.getChatListPhoneNumbers
    var getDistinctPhoneNumbers: LiveData<List<String>> = repository.getDistinctPhoneNumbers

    suspend fun readMessages(chatID: String): LiveData<List<Message>> {
        return repository.readMessages(chatID)
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

    fun createChatChannel(chatListItem: ChatListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createChatChannel(chatListItem)
        }
    }

    fun updateChatChannel(
       contact_number: String,
       messageID: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChatChannel(contact_number, messageID)
        }
    }

    suspend fun readSingleContact(phoneNumber: String): LiveData<TalksContact> {
        return repository.readSingleContact(phoneNumber)
    }
}