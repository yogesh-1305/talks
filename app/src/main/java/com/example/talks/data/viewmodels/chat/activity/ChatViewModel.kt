package com.example.talks.data.viewmodels.chat.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.constants.LocalConstants.MESSAGE_SENT
import com.example.talks.constants.ServerConstants
import com.example.talks.data.model.Message
import com.example.talks.data.model.Message.Companion.toTextMessage
import com.example.talks.data.model.TextMessage
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel
@Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val db: FirebaseFirestore
) : ViewModel() {

    fun sendMessage(message: Message, id_other: String, dbViewModel: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {

            dbViewModel.addMessage(message) // message status -> pending

            val textMessage = message.toTextMessage()
            val messageKey = message.creationTime.toString()

            db.collection(ServerConstants.FIREBASE_DB_NAME)
                .document(firebaseAuth.currentUser?.uid.toString())
                .collection("user_chats").document(messageKey).set(textMessage)
                .addOnSuccessListener {

                    // message status -> sent
                    dbViewModel.updateMessageStatus(MESSAGE_SENT, message.creationTime.toString())

                    setMessageToReceiverEnd(
                        messageKey,
                        id_other,
                        textMessage
                    )
                }
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

        db.collection(ServerConstants.FIREBASE_DB_NAME)
            .document(id_other)
            .collection("user_chats").document(messageKey).set(newMessage)
            .addOnSuccessListener {
                Log.i("message===", "set to rec end $it")
            }
    }



//    private var storageRef = FirebaseStorage.getInstance()
//    fun uploadImageToStorage(images: List<Uri>, userId: String?, time: String, context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            for (image in images) {
//                setImageMessageInDatabase(image, time)
//                val ref =
//                    storageRef.getReference(userId.toString()).child(UUID.randomUUID().toString())
//                val imageCompressed = Compressor.compress(context, image.toFile())
//                val task = ref.putFile(imageCompressed.toUri())
//                task.addOnSuccessListener {
//                    getDownloadUrl(task, ref)
//                }
//            }
//        }
//    }
//
//    private fun getDownloadUrl(uploadTask: UploadTask, reference: StorageReference) {
//        uploadTask.continueWithTask {
//            if (!it.isSuccessful) {
//                it.exception?.let { exception ->
//                    throw exception
//                }
//            }
//            reference.downloadUrl
//        }.addOnCompleteListener {
//            if (it.isSuccessful) {
//                Log.i("url+++", it.result.toString())
//            } else {
//                Log.i("url+++", it.exception?.message.toString())
//            }
//        }
//    }
//
//    private fun setImageMessageInDatabase(image: Uri, time: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val dbRef = Firebase.database.getReference("talks_database_chats")
//            val messageKey = dbRef.push().key.toString()
//
//            val message = Message(
//                receiverID.toString(), messageKey, "/image",
//            ).apply {
//                status = "offline"
//                creationTime = time
//                mediaLocalPath = image.path
//                sentByMe = true
//            }
//            databaseViewModel.addMessage(message)
//        }
//    }
//
//
//    private fun convertImageToString(image: File): String {
//        val bytes = Files.readAllBytes(Paths.get(image.toURI()))
//        return Base64.getEncoder().encodeToString(bytes)
//    }
//
//    private fun generateImageName(): String {
//        val date = CalendarManager.getDateForImage()
//        val id = Random().nextInt(99999)
//        return "IMG-$date-T$id"
//    }
//
//    private fun getImageSize(image: Uri, context: Context) {
//    }
//
//    private fun getBitmapFromFile(image: File): Bitmap {
//        return BitmapFactory.decodeFile(image.path)
//    }
//
//    private val talksFolder = File(Environment.getExternalStorageDirectory(), "Talks")
//    private fun saveImageInLocalStorage(image: Bitmap) {
//        if (talksFolder.exists()) {
//            val imageFile = File(
//                talksFolder.absolutePath + File.separator + "Sent Images",
//                "${generateImageName()}.jpeg"
//            )
//
//            if (!imageFile.exists()) {
//                try {
//                    val outputStream = FileOutputStream(imageFile)
//                    image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                    outputStream.flush()
//                    outputStream.close()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//
//    }


}