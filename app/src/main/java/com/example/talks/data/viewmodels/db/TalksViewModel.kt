package com.example.talks.data.viewmodels.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.data.model.*
import com.example.talks.data.repositories.db.TalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TalksViewModel @Inject public constructor(private val repository: TalksRepository) : ViewModel() {

    val readAllUserData: LiveData<List<User>> = repository.readAllUserData
    val readAllContacts: LiveData<List<TalksContact>> = repository.readContacts
    val readChatListItem: LiveData<List<ChatListQueriedData>> = repository.readChatListItem
    val readContactPhoneNumbers: LiveData<List<String>> = repository.readContactPhoneNumbers
    val getDistinctMessages: LiveData<List<String>> = repository.getDistinctMessages
    val lastAddedMessage: LiveData<Message> = repository.lastAddedMessage
    val getChatListPhoneNumbers: LiveData<List<String>> = repository.getChatListPhoneNumbers
    val getDistinctPhoneNumbers: LiveData<List<String>> = repository.getDistinctPhoneNumbers

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

    fun updateUserImage(userImage: String, imageLocalPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserImage(userImage, imageLocalPath)
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

    suspend fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact> {
        return repository.readSingleContact(userPhoneNumber)
    }
}