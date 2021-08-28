package com.example.talks.signup.thirdFragment

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThirdFragmentViewModel @Inject constructor(var storageRef: FirebaseStorage) :
    ViewModel() {
//    private val storageRef = FirebaseStorage.getInstance()

    val existingUserData: MutableLiveData<HashMap<String, String>> by lazy {
        MutableLiveData<HashMap<String, String>>()
    }
    val localUserData: MutableLiveData<HashMap<String, String?>> by lazy {
        MutableLiveData<HashMap<String, String?>>()
    }
    val profileImageUrl: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    fun getUserFromDatabaseIfExists(userUid: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database")
            if (userUid != null) {
                dbRef.child(userUid).get().addOnSuccessListener {
                    val userName = it.child("user_name").value.toString()
                    val userImage = it.child("user_image_url").value.toString()
                    val userBio = it.child("user_bio").value.toString()

                    val user = hashMapOf(
                        "user_name" to userName,
                        "user_image" to userImage,
                        "user_bio" to userBio
                    )
                    existingUserData.value = user
                }
            }
        }
    }

    fun addUserToFirebaseDatabase(
        user: HashMap<String, String?>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database")
            if (user["user_UID"] != null) {
                dbRef.child(user["user_UID"]!!).setValue(user).addOnSuccessListener {
                    val localUser = hashMapOf(
                        "phone_number" to user["phone_number"],
                        "user_name" to user["user_name"],
                        "user_image" to user["user_image_url"],
                        "user_bio" to user["user_bio"],
                        "user_id" to user["user_UID"]
                    )
                    localUserData.value = localUser
                }.addOnFailureListener {
                    Log.i("failed login====", it.toString())
                }
            }
        }
    }


    fun uploadImageToStorage(image: Uri?, userId: String) {
        if (image != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val reference = storageRef.getReference(userId).child("profile_image")
                val uploadTask = reference.putFile(image)
                uploadTask.addOnSuccessListener {
                    getDownloadUrl(uploadTask, reference)
                }
            }
        }
    }

    private fun getDownloadUrl(uploadTask: UploadTask, reference: StorageReference) {
        uploadTask.continueWithTask {
            if (!it.isSuccessful) {
                it.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                profileImageUrl.value = it.result.toString()
            } else {
                profileImageUrl.value = it.exception?.message.toString()
            }
        }
    }
}