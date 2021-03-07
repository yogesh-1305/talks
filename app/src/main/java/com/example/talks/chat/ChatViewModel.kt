package com.example.talks.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.modal.MessageSchema
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val fireStore: FirebaseFirestore = Firebase.firestore

    fun sendMessage(currentUserUid: String?, uId: String, message: MessageSchema) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserUid != null) {
                fireStore.collection("chat_database").document(currentUserUid)
                    .collection(uId).document()
                    .set(message).addOnSuccessListener {
                        setMessageToReceiverEnd(uId, currentUserUid, message)
                    }
            }
        }
    }

    private fun setMessageToReceiverEnd(
        receiverId: String,
        currentUserUid: String,
        message: MessageSchema
    ) {
        fireStore.collection("chat_database").document(receiverId)
            .collection(currentUserUid).document().set(message).addOnSuccessListener {
                setLatestMessage(currentUserUid, receiverId, message)
            }

    }

    private fun setLatestMessage(currentUserUid: String?, uId: String, message: MessageSchema) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserUid != null) {
                fireStore.collection("chat_database").document(currentUserUid)
                    .collection(uId).document("latest_message").set(message)
                    .addOnSuccessListener {
                        setLatestMessageToReceiverEnd(currentUserUid, uId, message)
                    }
            }
        }
    }

    private fun setLatestMessageToReceiverEnd(
        currentUserUid: String?,
        uId: String,
        message: MessageSchema
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserUid != null) {
                fireStore.collection("chat_database").document(uId)
                    .collection(currentUserUid).document("latest_message").set(message)
            }

        }
    }
}