package com.example.talks.data.viewmodels.authentication.fragments

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.others.encryption.Encryption
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
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
    @DelicateCoroutinesApi
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

                        val contactNumber = data.child("phone_number").value.toString()
                        val contactUserName = data.child("user_name").value.toString()
                        val contactImageUrl = data.child("user_image_url").value.toString()
                        val contactBio = data.child("user_bio").value.toString()
                        val contactId = data.child("userUID").value.toString()

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