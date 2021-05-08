package com.example.talks.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.Message
import com.example.talks.database.TalksViewModel
import com.example.talks.modal.DBMessage
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messagesList: MutableLiveData<MutableList<DBMessage>> by lazy {
        MutableLiveData<MutableList<DBMessage>>()
    }

    val messageArrayList = ArrayList<DBMessage>()


    fun readMessagesFromServer(userId: String?, receiverID: String, talksVM: TalksViewModel) {
        val dbRef = Firebase.database.getReference("talks_database_chats")
        if (userId != null) {
            dbRef.child(userId).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(DBMessage::class.java)
                    val key = snapshot.key.toString()

                    val chatId = message?.chatId
                    val messageText = message?.messageText
                    val messageStatus = message?.messageStatus
                    val sendTime = message?.sendTime
                    val sendDate = message?.sendDate

                    val localMessage = Message(
                        chatId.toString(), key, messageText.toString(),
                        messageStatus.toString(),
                        true, sendTime.toString(),
                        sendDate.toString(), false
                    )

                    talksVM.addMessage(localMessage)

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

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
                "$receiverID@talks.net", messageKey, messageText, "offline",
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
            "$senderID@talks.net",
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