package com.example.talks.home.activity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.R
import com.example.talks.database.ChatListItem
import com.example.talks.database.Message
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.encryption.Encryption
import com.example.talks.modal.DBMessage
import com.example.talks.modal.ServerUser
import com.example.talks.receiver.ActionReceiver
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel : ViewModel() {
    private var fireStore: FirebaseFirestore = Firebase.firestore

    val users: MutableLiveData<MutableList<ServerUser>> by lazy {
        MutableLiveData<MutableList<ServerUser>>()
    }

    val chatListItem: MutableLiveData<ChatListItem> by lazy {
        MutableLiveData<ChatListItem>()
    }

    fun getUsersFromServer(
        databaseContactList: List<String>,
        contactNameList: HashMap<String, String>,
        databaseViewModel: TalksViewModel,
        encryptionKey: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            Firebase.database.getReference("talks_database").get()
                .addOnSuccessListener {
//                    Log.i("contacts Snapshot====", it.toString())

                    for (data in it.children) {

                        val contactNumber = data.child("contactNumber").value.toString()
                        val contactUserName = data.child("contactUserName").value.toString()
                        val contactImageUrl = data.child("contactImageUrl").value.toString()
                        val contactBio = data.child("contact_bio").value.toString()
                        val contactStatus = data.child("status").value.toString()
                        val contactId = data.child("uid").value.toString()
                        val contactImageBitmap = data.child("contactImageBitmap").value.toString()
//                        Log.i("contact number in homeVM****", data.toString())
                        if (databaseContactList.contains(contactNumber)) {
//                            Log.i("contacts====", contactNumber)
                            val decryptedImage =
                                Encryption().decrypt(contactImageUrl, encryptionKey)
                            val user = TalksContact(
                                contactNumber,
                                true,
                                contactNameList[contactNumber],
                                contactUserName,
                                decryptedImage,
                                contactImageBitmap,
                                contactId,
                                contactStatus,
                                contactBio
                            )
                            databaseViewModel.updateUser(user)
                        }
                    }
                }
        }
    }

    fun getUserDataFromServer(phoneNumber: String) {
        val dbRef = Firebase.database.getReference("talks_database")
        dbRef.child(phoneNumber).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getCurrentUserData(currentUserId: String?, databaseViewModel: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserId != null) {
                fireStore.collection("user_database").document(currentUserId)
                    .get().addOnSuccessListener {
                        val currentUser = it.toObject<ServerUser>()
                        if (currentUser != null) {
//                            val localUser = TalksContact(
//                                currentUser.getUserPhoneNumber(),
//                                currentUser.
//                                currentUser.getUserName(),
//                                currentUser.getUserProfileImage(),
//                                currentUserId
//                            )
//                            databaseViewModel.updateUser(localUser)
                        }
                    }
            }
        }
    }


    fun readMessagesFromServer(userId: String?, talksVM: TalksViewModel) {
        val dbRef = Firebase.database.getReference("talks_database_chats")
        if (userId != null) {
            dbRef.child(userId).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(DBMessage::class.java)
                    val key = snapshot.key.toString()

                    val chatId = message?.chatId
                    val messageText = message?.messageText
                    val messageStatus = message?.messageStatus
                    val needsPush = message?.needsPush
                    val sendTime = message?.sendTime
                    val sendDate = message?.sendDate
                    val sentByMe = message?.sentByMe

                    val localMessage = Message(
                        chatId.toString(), key, messageText.toString(),
                        messageStatus.toString(),
                        needsPush, sendTime.toString(),
                        sendDate.toString(), sentByMe
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

    val peerConnectionID: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun readPeerConnections(context: Context, currentUserID: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserID != null) {
                Firebase.database.getReference("talks_database").child(currentUserID)
                    .child("call_stats").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (data in snapshot.children) {
                                if (data.key.toString() == "callerID") {
                                    if (data.value.toString() != "") {
                                        createNotificationForCall(context)

                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
                        }
                    })
            }
        }
    }

    private fun createNotificationForCall(context: Context) {
        val declineIntent = Intent(context, ActionReceiver::class.java)
        val acceptIntent = Intent(context, ActionReceiver::class.java)

        declineIntent.putExtra("action", 0)
        acceptIntent.putExtra("action", 1)

        val pendingIntentDecline = PendingIntent.getBroadcast(context, 0, declineIntent, 0)
        val pendingIntentAccept = PendingIntent.getBroadcast(context, 1, acceptIntent, 0)

        val builder = NotificationCompat.Builder(context, "Calls")
            .setSmallIcon(R.drawable.bell_icon)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .addAction(R.drawable.video_call_icon, "Decline", pendingIntentDecline)
            .addAction(R.drawable.video_call_icon, "Accept", pendingIntentAccept)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())
    }
}