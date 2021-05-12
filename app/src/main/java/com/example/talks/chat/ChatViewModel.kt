package com.example.talks.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.Message
import com.example.talks.database.TalksViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    fun sendMessage(
        senderID: String?,
        receiverID: String,
        messageText: String,
        time: String,
        date: String,
        databaseViewModel: TalksViewModel
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            val dbRef = Firebase.database.getReference("talks_database_chats")
            val messageKey = dbRef.push().key.toString()

            val message = Message(
                receiverID, messageKey, messageText, "offline",
                false, time,
                date, true
            )

            databaseViewModel.addMessage(message)

            dbRef.child(senderID.toString()).child(messageKey).setValue(message)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        Log.i("message===", "sent")
                        setMessageToReceiverEnd(
                            dbRef,
                            receiverID,
                            senderID.toString(),
                            messageText,
                            time,
                            date
                        )
                    } else {
                        Log.i("result===", it.result.toString())
                    }
                }
        }
    }

    private fun setMessageToReceiverEnd(
        dbRef: DatabaseReference,
        receiverID: String,
        senderID: String,
        messageText: String,
        time: String,
        date: String
    ) {

        val key = dbRef.push().key
        val message = Message(
            senderID,
            key,
            messageText,
            "received",
            true,
            time,
            date,
            false
        )

        if (key != null) {
            dbRef.child(receiverID).child(key).setValue(message)
                .addOnSuccessListener {
                    Log.i("message===", "set to rec end $it")
                }
        }
    }
}