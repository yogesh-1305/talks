package com.example.talks.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.talks.database.TalksViewModel
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory(
    private val senderID: String?,
    private val receiverID: String?,
    private val databaseViewModel: TalksViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(senderID, receiverID, databaseViewModel) as T
        }
        throw IllegalArgumentException("ViewModel Not Found (chat ViewModelFactory)")
    }
}