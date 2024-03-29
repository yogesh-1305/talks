package com.example.talks.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private var fireStore: FirebaseFirestore = Firebase.firestore
    private var storageRef = FirebaseStorage.getInstance()

    val updatedProfileImageURL: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val imageUpdatedInLocalDatabase: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun updateProfileImageInStorage(image: Uri?, uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val reference = storageRef.getReference(uid).child("$uid profile_image")
            val uploadTask = reference.putFile(image!!)
            uploadTask.addOnSuccessListener {
                getDownloadUrl(uploadTask, reference)
            }
        }
    }

    private fun getDownloadUrl(uploadTask: UploadTask, reference: StorageReference) {
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                updatedProfileImageURL.value = it.result.toString()
            } else {
                updatedProfileImageURL.value = null
            }
        }
    }

    fun updateImageToDatabase(imageUrl: String, imageLocalPath: String, uid: String?, databaseViewModel: TalksViewModel) {
        val dbRef = Firebase.database.getReference("talks_database")
        if (uid != null) {
            val imageUpdate: MutableMap<String, String> = HashMap()
            imageUpdate["contactImageUrl"] = imageUrl.toString()

            dbRef.child(uid).updateChildren(imageUpdate as Map<String, String>)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        databaseViewModel.updateUserImage(imageUrl, imageLocalPath)
                        imageUpdatedInLocalDatabase.value = true
                    }
                }
        }
    }

}