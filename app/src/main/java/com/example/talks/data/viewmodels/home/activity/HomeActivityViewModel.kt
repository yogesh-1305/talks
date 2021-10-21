package com.example.talks.data.viewmodels.home.activity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.R
import com.example.talks.constants.ServerConstants
import com.example.talks.constants.ServerConstants.FIREBASE_DB_NAME
import com.example.talks.data.model.ChatListItem
import com.example.talks.data.model.Message
import com.example.talks.data.model.TalksContact
import com.example.talks.data.model.TextMessage
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.others.encryption.Encryption
import com.example.talks.data.receiver.ActionReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class HomeActivityViewModel
@Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val db: FirebaseFirestore
) : ViewModel() {

    val chatListItem: MutableLiveData<ChatListItem> by lazy {
        MutableLiveData<ChatListItem>()
    }

    var testValue = 0

    @RequiresApi(Build.VERSION_CODES.O)
    @DelicateCoroutinesApi
    fun getUsersFromServer(
        databaseContactList: List<String>,
        contactNameList: HashMap<String, String>,
        databaseViewModel: TalksViewModel,
        encryptionKey: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            db.collection(FIREBASE_DB_NAME).get().addOnSuccessListener {
                db.collection(FIREBASE_DB_NAME).get().addOnSuccessListener {
                    for (document in it.documents) {
                        val contactNumber =
                            document.get(ServerConstants.USER_PHONE_NUMBER).toString()
                        val contactUserName =
                            document.get(ServerConstants.USER_NAME).toString()
                        val contactImageUrl =
                            document.get(ServerConstants.USER_IMAGE_URL).toString()
                        val contactBio = document.get(ServerConstants.USER_BIO).toString()
                        val contactId =
                            document.get(ServerConstants.USER_UNIQUE_ID).toString()

                        val decryptedImage =
                            Encryption().decrypt(contactImageUrl, encryptionKey)
                        val user = TalksContact(
                            contactNumber,
                            contactNameList[contactNumber]
                        ).apply {
                            this.contactUserName = contactUserName
                            this.contactImageUrl = decryptedImage.toString()
                            this.uId = contactId
                            this.isTalksUser = true
                            this.contactBio = contactBio
                        }
                        databaseViewModel.updateUser(user)
                    }
                }
            }

//            Firebase.database.getReference("talks_database").get()
//                .addOnSuccessListener {
//
//                    for (data in it.children) {
//
//                        val contactNumber = data.child("phone_number").value.toString()
//                        val contactUserName = data.child("user_name").value.toString()
//                        val contactImageUrl = data.child("user_image_url").value.toString()
//                        val contactBio = data.child("user_bio").value.toString()
//                        val contactId = data.child("userUID").value.toString()
//
//                        val decryptedImage =
//                            Encryption().decrypt(contactImageUrl, encryptionKey)
//                        val user = TalksContact(
//                            contactNumber,
//                            contactNameList[contactNumber]
//                        ).apply {
//                            this.contactUserName = contactUserName
//                            this.contactImageUrl = decryptedImage.toString()
//                            this.uId = contactId
//                            this.isTalksUser = true
//                            this.contactBio = contactBio
//                        }
//                        databaseViewModel.updateUser(user)
//
//                    }
//                }
        }
    }

//    fun getUserDataFromServer(phoneNumber: String) {
//        val dbRef = Firebase.database.getReference("talks_database")
//        dbRef.child(phoneNumber).addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
//    }


    fun readMessagesFromServer(userId: String?, talksVM: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {

            firebaseAuth.currentUser?.let {
                db.collection(FIREBASE_DB_NAME).document(it.uid).collection("user_chats")
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            for (document in snapshot.documents){
                                val message = document.toObject(Message::class.java)
                                if (message != null) {
                                    talksVM.addMessage(message)
                                }
                            }

                        }
                    }
            }
        }

//            val dbRef = Firebase.database.getReference("talks_database_chats")
//            if (userId != null) {
//                dbRef.child(userId).addChildEventListener(object : ChildEventListener {
//                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//
//                            val chatId = snapshot.child("chatId").value.toString()
//                            val messageID = snapshot.key.toString()
//                            val messageType = snapshot.child("messageType").value.toString()
//                            val messageText = snapshot.child("messageText").value.toString()
//                            val status = snapshot.child("status").value.toString()
//                            val messageCreationTime =
//                                snapshot.child("creationTime").value.toString()
//                            val deliveryTime = snapshot.child("deliveryTime").value.toString()
//                            val seenTime = snapshot.child("seenTime").value.toString()
//                            val mediaSize = snapshot.child("mediaSize").value.toString()
//                            val mediaDuration = snapshot.child("mediaDuration").value.toString()
//                            val mediaCaption = snapshot.child("mediaCaption").value.toString()
//                            val mediaUrl = snapshot.child("mediaUrl").value.toString()
//                            val mediaThumbnailString =
//                                snapshot.child("mediaThumbnailString").value.toString()
//                            val sentByMe = snapshot.child("sentByMe").value as Boolean?
//                            val deleted = snapshot.child("deleted").value as Boolean?
//
//                            val message = Message(
//                                id = 0,
//                                chatId = chatId,
//                                messageID = messageID,
//                                messageType = messageType,
//                                messageText = messageText,
//                                status = status,
//                                creationTime = messageCreationTime,
//                                deliveryTime = deliveryTime,
//                                seenTime = seenTime,
//                                mediaSize = mediaSize,
//                                mediaDuration = mediaDuration,
//                                mediaCaption = mediaCaption,
//                                mediaUrl = mediaUrl,
//                                mediaThumbnailString = mediaThumbnailString,
//                                sentByMe = sentByMe,
//                                deleted = deleted
//                            )
//                            talksVM.addMessage(message)
//                    }
//
//                    override fun onChildChanged(
//                        snapshot: DataSnapshot,
//                        previousChildName: String?
//                    ) {
//                        val status = snapshot.child("status").value.toString()
//                        val deliveryTime = snapshot.child("deliveryTime").value.toString()
//                        val seenTime = snapshot.child("seenTime").value.toString()
//                        val deleted = snapshot.child("deleted").value as Boolean?
//                    }
//
//                    override fun onChildRemoved(snapshot: DataSnapshot) {
//                    }
//
//                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
//            }
//        }

    }

    fun sendMessage(message: TextMessage, id_other: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val messageKey = message.creationTime.toString()
            db.collection(FIREBASE_DB_NAME)
                .document(firebaseAuth.currentUser?.uid.toString())
                .collection("user_chats").document(messageKey).set(message)
                .addOnSuccessListener {
                    setMessageToReceiverEnd(
                        messageKey,
                        id_other,
                        message
                    )
                }

//            dbReference.child(firebaseAuth.currentUser?.phoneNumber.toString()).child(messageKey)
//                .setValue(message)
//                .addOnCompleteListener {
//                    if (it.isComplete) {
//                        setMessageToReceiverEnd(
//                            messageKey,
//                            id_other,
//                            message
//                        )
//                    } else {
//                        Log.i("result===", it.result.toString())
//                    }
//                }
        }
    }

    private fun setMessageToReceiverEnd(
        messageKey: String,
        id_other: String,
        message: TextMessage
    ) {

        val newMessage = message.copy(
            chatId = firebaseAuth.currentUser?.phoneNumber.toString(),
            status = "received",
            sentByMe = false
        )

        db.collection(FIREBASE_DB_NAME)
            .document(id_other)
            .collection("user_chats").document(messageKey).set(newMessage)
            .addOnSuccessListener {
                Log.i("message===", "set to rec end $it")
            }
//            dbReference.child(id_other).child(messageKey)
//                .setValue(newMessage)
//                .addOnSuccessListener {
//                    Log.i("message===", "set to rec end $it")
//                }
    }

    val contactData: MutableLiveData<TalksContact> by lazy {
        MutableLiveData<TalksContact>()
    }

//fun readPeerConnections(
//    context: Context,
//    currentUserID: String?,
//    contactNames: HashMap<String, String>
//) {
//    viewModelScope.launch(Dispatchers.IO) {
//        if (currentUserID != null) {
//            Firebase.database.getReference("talks_database").child(currentUserID)
//                .child("call_stats").addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val callerID = snapshot.child("callerID").value.toString()
//                        val callerPhoneNumber =
//                            snapshot.child("callerPhoneNumber").value.toString()
//                        val callerName = getCallerName(contactNames, callerPhoneNumber)
//                        if (callerPhoneNumber != "") {
//                            createNotificationForCall(context, callerName)
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
////                            TODO("Not yet implemented")
//                    }
//                })
//        }
//    }
//}

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

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())
    }

}