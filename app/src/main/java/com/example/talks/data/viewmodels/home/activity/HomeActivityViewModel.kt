package com.example.talks.data.viewmodels.home.activity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.R
import com.example.talks.constants.ServerConstants
import com.example.talks.constants.ServerConstants.FIREBASE_CHATS_DB_NAME
import com.example.talks.constants.ServerConstants.FIREBASE_DB_NAME
import com.example.talks.data.model.ChatListItem
import com.example.talks.data.model.Message
import com.example.talks.data.model.TalksContact
import com.example.talks.data.receiver.ActionReceiver
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.others.encryption.Encryption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeActivityViewModel
@Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val db: FirebaseFirestore,
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
        encryptionKey: String,
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
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readMessagesFromServer(talksVM: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val latestMessageCreationTime = talksVM.getLastMessageCreationTime()

            firebaseAuth.currentUser?.let {
                db.collection(FIREBASE_DB_NAME).document(it.uid).collection(FIREBASE_CHATS_DB_NAME)
                    .whereGreaterThanOrEqualTo("creationTime", latestMessageCreationTime ?: "")
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            for (document in snapshot.documents) {
                                val message = document.toObject(Message::class.java)
                                if (message != null) {
                                    talksVM.addMessage(message)
                                    updateDeliveryTimeInFirebase(message)
                                }
                            }
                        }
                    }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            firebaseAuth.currentUser?.let {
                db.collection(FIREBASE_DB_NAME).document(it.uid).collection(FIREBASE_CHATS_DB_NAME)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {

                            for (changes in snapshot.documentChanges) {

                                when (changes.type) {
                                    DocumentChange.Type.MODIFIED -> {
                                        val creationTime =
                                            changes.document["creationTime"].toString()
                                        val deliveryTime =
                                            changes.document["deliveryTime"].toString()
                                        val seenTime = changes.document["seenTime"].toString()

                                        if (deliveryTime.isNotEmpty()) {
                                            // update delivery time in db
                                            talksVM.updateMessageDeliveryTime(deliveryTime,
                                                creationTime)
                                        }
                                        if (seenTime.isNotEmpty()) {
                                            // update seen time in db
                                            talksVM.updateMessageSeenTime(seenTime, creationTime)
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        TODO("handle message removal")
                                    }
                                    else -> { /* NO-OP */}
                                }
                            }
                        }
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDeliveryTimeInFirebase(message: Message?) {
        val ref = db.collection(FIREBASE_DB_NAME).document(message?.senderID.toString()).collection(
            FIREBASE_CHATS_DB_NAME).document()

        db.runBatch { batch ->
            batch.update(ref, "deliveryTime", LocalDateTime.now().toString())
        }
    }

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
    contactNames: HashMap<String, String>, callerID: String,
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
