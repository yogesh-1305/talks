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
import com.example.talks.modal.ServerUser
import com.example.talks.receiver.ActionReceiver
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel : ViewModel() {

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

                    for (data in it.children) {

                        val contactNumber = data.child("contactNumber").value.toString()
                        val contactUserName = data.child("contactUserName").value.toString()
                        val contactImageUrl = data.child("contactImageUrl").value.toString()
                        val contactBio = data.child("contact_bio").value.toString()
                        val contactStatus = data.child("status").value.toString()
                        val contactId = data.child("uid").value.toString()
                        val contactImageBitmap = data.child("contactImageBitmap").value.toString()

                        if (databaseContactList.contains(contactNumber)) {

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


    fun readMessagesFromServer(userId: String?, talksVM: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database_chats")
            if (userId != null) {
                dbRef.child(userId).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                        val chatId = snapshot.child("chatId").value.toString()
                        val messageID = snapshot.key.toString()
                        val messageType = snapshot.child("messageType").value.toString()
                        val messageText = snapshot.child("messageText").value.toString()
                        val status = snapshot.child("status").value.toString()
                        val messageCreationTime = snapshot.child("creationTime").value.toString()
                        val deliveryTime = snapshot.child("deliveryTime").value.toString()
                        val seenTime = snapshot.child("seenTime").value.toString()
                        val mediaSize = snapshot.child("mediaSize").value.toString()
                        val mediaDuration = snapshot.child("mediaDuration").value.toString()
                        val mediaCaption = snapshot.child("mediaCaption").value.toString()
                        val mediaUrl = snapshot.child("mediaUrl").value.toString()
                        val mediaThumbnailString = snapshot.child("mediaThumbnailString").value.toString()
                        val sentByMe = snapshot.child("sentByMe").value as Boolean?
                        val deleted = snapshot.child("deleted").value as Boolean?

                        val localMessage = Message(
                            chatId,
                            messageID,
                            messageType
                        ).apply {
                            this.messageText = messageText
                            this.status = status
                            this.creationTime = messageCreationTime
                            this.deliveryTime = deliveryTime
                            this.seenTime = seenTime
                            this.mediaSize = mediaSize
                            this.mediaDuration = mediaDuration
                            this.mediaCaption = mediaCaption
                            this.mediaUrl = mediaUrl
                            this.mediaThumbnailString = mediaThumbnailString
                            this.sentByMe = sentByMe
                            this.deleted = deleted
                        }
                        talksVM.addMessage(localMessage)

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val status = snapshot.child("status").value.toString()
                        val deliveryTime = snapshot.child("deliveryTime").value.toString()
                        val seenTime = snapshot.child("seenTime").value.toString()
                        val deleted = snapshot.child("deleted").value as Boolean?
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

    }

    val contactData: MutableLiveData<TalksContact> by lazy {
        MutableLiveData<TalksContact>()
    }

    fun readPeerConnections(
        context: Context,
        currentUserID: String?,
        contactNames: HashMap<String, String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserID != null) {
                Firebase.database.getReference("talks_database").child(currentUserID)
                    .child("call_stats").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val callerID = snapshot.child("callerID").value.toString()
                            val callerPhoneNumber =
                                snapshot.child("callerPhoneNumber").value.toString()
                            val callerName = getCallerName(contactNames, callerPhoneNumber)
                            if (callerPhoneNumber != "") {
                                createNotificationForCall(context, callerName)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
                        }
                    })
            }
        }
    }

    private fun getCallerName(
        contactNames: HashMap<String, String>, callerID: String
    ): String {
        return if (contactNames.containsKey(callerID)) {
            contactNames[callerID].toString()
        } else {
            callerID
        }
    }

    private fun createNotificationForCall(context: Context, callerName: String) {
        val declineIntent = Intent(context, ActionReceiver::class.java)
        val acceptIntent = Intent(context, ActionReceiver::class.java)

        declineIntent.putExtra("action", 0)
        acceptIntent.putExtra("action", 1)

        val pendingIntentDecline = PendingIntent.getBroadcast(context, 0, declineIntent, 0)
        val pendingIntentAccept = PendingIntent.getBroadcast(context, 1, acceptIntent, 0)

        val builder = NotificationCompat.Builder(context, "Calls")
            .setSmallIcon(R.drawable.bell_icon)
            .setContentTitle(callerName)
            .setContentText("Incoming Video Call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .addAction(R.drawable.video_call_icon, "Decline", pendingIntentDecline)
            .addAction(R.drawable.video_call_icon, "Accept", pendingIntentAccept)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())
    }
}