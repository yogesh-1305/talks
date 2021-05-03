package com.example.talks.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    fun sendMessage(
        senderID: String?,
        receiverID: String,
        message: Message,
        fireStore: FirebaseFirestore
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            Log.i("current user check null ===", senderID.toString())

            if (senderID != null) {
                fireStore.collection("chat_database").document(senderID)
                    .collection(receiverID).document()
                    .set(message).addOnSuccessListener {
                        setMessageToReceiverEnd(fireStore, receiverID, senderID, message)
                        Log.i("message check===", message.messageText)
                        Log.i("current user check===", senderID)
                    }
            }
        }
    }

    private fun setMessageToReceiverEnd(
        fireStore: FirebaseFirestore,
        receiverID: String,
        senderID: String,
        message: Message
    ) {
        fireStore.collection("chat_database").document(receiverID)
            .collection(senderID).document().set(message).addOnSuccessListener {
                setLatestMessage(fireStore, senderID, receiverID, message)
            }

    }

    private fun setLatestMessage(
        fireStore: FirebaseFirestore,
        senderID: String?,
        receiverID: String,
        message: Message
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (senderID != null) {
                fireStore.collection("chat_database").document(senderID)
                    .collection(receiverID).document("latest_message").set(message)
                    .addOnSuccessListener {
                        setLatestMessageToReceiverEnd(fireStore, senderID, receiverID, message)
                    }
            }
        }
    }

    private fun setLatestMessageToReceiverEnd(
        fireStore: FirebaseFirestore,
        senderID: String?,
        receiverID: String,
        message: Message
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (senderID != null) {
                fireStore.collection("chat_database").document(receiverID)
                    .collection(senderID).document("latest_message").set(message)
            }

        }
    }
}