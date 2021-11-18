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
class TalksViewModel @Inject constructor(private val repository: TalksRepository) :
    ViewModel() {

    val readAllUserData: LiveData<List<User>> = repository.readAllUserData
    val readAllContacts: LiveData<List<TalksContact>> = repository.readContacts
    val readHomeScreenChannelList: LiveData<List<HomeScreenChannelList>> =
        repository.readHomeScreenChannelList
    val readContactPhoneNumbers: LiveData<List<String>> = repository.readContactPhoneNumbers
    val lastAddedMessage: LiveData<Message> = repository.lastAddedMessage
    val getChatListPhoneNumbers: LiveData<List<String>> = repository.getChatListPhoneNumbers

    fun readMessages(chatID: String): LiveData<List<Message>> {
        return repository.readMessages(chatID)
    }

    suspend fun getMessagesDataForChatList() {
        repository.getMessagesDataForChatList()
    }

    suspend fun getLastMessageCreationTime(): String {
        return repository.getLastMessageCreationTime()
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

    fun updateMessageStatus(status: String, creationTime: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMessageStatus(status, creationTime)
        }
    }

    fun createChatChannel(chatListItem: ChatListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createChatChannel(chatListItem)
        }
    }

    fun createChatChannels(list: List<ChatListItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createChatChannels(list)
        }
    }

    fun updateChatListLatestMessage(
        contact_number: String,
        latestMessageId: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChatListLatestMessage(contact_number, latestMessageId)
        }
    }

    fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact> {
        return repository.readSingleContact(userPhoneNumber)
    }
}