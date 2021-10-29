package com.example.talks.data.viewmodels.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.data.model.*
import com.example.talks.data.repositories.db.TalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TalksViewModel @Inject public constructor(private val repository: TalksRepository) :
    ViewModel() {

    val readAllUserData: LiveData<List<User>> = repository.readAllUserData
    val readAllContacts: LiveData<List<TalksContact>> = repository.readContacts
    val readChatListItem: LiveData<List<ChatListItem>> = repository.readChatListItem
    val readContactPhoneNumbers: LiveData<List<String>> = repository.readContactPhoneNumbers
    val getDistinctMessages: LiveData<List<String>> = repository.getDistinctMessages
    val lastAddedMessage: LiveData<Message> = repository.lastAddedMessage
    val getChatListPhoneNumbers: LiveData<List<String>> = repository.getChatListPhoneNumbers
    val getDistinctPhoneNumbers: LiveData<List<String>> = repository.getDistinctPhoneNumbers

    val messagesDataForChatList: MutableList<ChatListQueriedData> = ArrayList()

    suspend fun readMessages(chatID: String): LiveData<List<Message>> {
        return repository.readMessages(chatID)
    }

    fun getMessagesDataForChatList() {
        viewModelScope.launch {
            val data = repository.getMessagesDataForChatList()
            messagesDataForChatList.addAll(data)
        }
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

    fun updateChatListLatestMessage(
        contact_number: String,
        latestMessageId: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChatListLatestMessage(contact_number, latestMessageId)
        }
    }

    suspend fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact> {
        return repository.readSingleContact(userPhoneNumber)
    }
}