package com.example.talks.data.viewmodels.chat.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.others.calendar.CalendarManager
import com.example.talks.data.model.Message
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class ChatViewModel(
    private val senderID: String?,
    private val receiverID: String?,
    val databaseViewModel: TalksViewModel
) : ViewModel() {

    fun sendMessage(text: String, time: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (senderID != null && receiverID != null) {

                val dbRef = Firebase.database.getReference("talks_database_chats")
                val messageKey = dbRef.push().key.toString()

                val message = Message(
                    receiverID, messageKey, "/text",
                ).apply {
                    messageText = text
                    status = "offline"
                    creationTime = time
                    sentByMe = true
                }
                databaseViewModel.addMessage(message)

                dbRef.child(senderID.toString()).child(messageKey).setValue(message)
                    .addOnCompleteListener {
                        if (it.isComplete) {
                            setMessageToReceiverEnd(
                                messageKey,
                                dbRef,
                                text,
                                time
                            )
                        } else {
                            Log.i("result===", it.result.toString())
                        }
                    }
            }
        }
    }

    private fun setMessageToReceiverEnd(
        messageKey: String,
        dbRef: DatabaseReference,
        text: String,
        time: String
    ) {

        if (senderID != null && receiverID != null) {
            val message = Message(
                senderID, messageKey, "/text",
            ).apply {
                messageText = text
                status = "received"
                creationTime = time
                sentByMe = false
            }
            dbRef.child(receiverID).child(messageKey).setValue(message)
                .addOnSuccessListener {
                    Log.i("message===", "set to rec end $it")
                }
        }

    }


    private var storageRef = FirebaseStorage.getInstance()
    fun uploadImageToStorage(images: List<Uri>, userId: String?, time: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            for (image in images) {
                setImageMessageInDatabase(image, time)
                val ref =
                    storageRef.getReference(userId.toString()).child(UUID.randomUUID().toString())
                val imageCompressed = Compressor.compress(context, image.toFile())
                val task = ref.putFile(imageCompressed.toUri())
                task.addOnSuccessListener {
                    getDownloadUrl(task, ref)
                }
            }
        }
    }

    private fun getDownloadUrl(uploadTask: UploadTask, reference: StorageReference) {
        uploadTask.continueWithTask {
            if (!it.isSuccessful) {
                it.exception?.let { exception ->
                    throw exception
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("url+++", it.result.toString())
            } else {
                Log.i("url+++", it.exception?.message.toString())
            }
        }
    }

    private fun setImageMessageInDatabase(image: Uri, time: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database_chats")
            val messageKey = dbRef.push().key.toString()

            val message = Message(
                receiverID.toString(), messageKey, "/image",
            ).apply {
                status = "offline"
                creationTime = time
                mediaLocalPath = image.path
                sentByMe = true
            }
            databaseViewModel.addMessage(message)
        }
    }


    private fun convertImageToString(image: File): String {
        val bytes = Files.readAllBytes(Paths.get(image.toURI()))
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun generateImageName(): String {
        val date = CalendarManager.getDateForImage()
        val id = Random().nextInt(99999)
        return "IMG-$date-T$id"
    }

    private fun getImageSize(image: Uri, context: Context) {
    }

    private fun getBitmapFromFile(image: File): Bitmap {
        return BitmapFactory.decodeFile(image.path)
    }

    private val talksFolder = File(Environment.getExternalStorageDirectory(), "Talks")
    private fun saveImageInLocalStorage(image: Bitmap) {
        if (talksFolder.exists()) {
            val imageFile = File(
                talksFolder.absolutePath + File.separator + "Sent Images",
                "${generateImageName()}.jpeg"
            )

            if (!imageFile.exists()) {
                try {
                    val outputStream = FileOutputStream(imageFile)
                    image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }


}