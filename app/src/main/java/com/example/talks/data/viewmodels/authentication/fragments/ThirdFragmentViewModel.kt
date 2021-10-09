package com.example.talks.data.viewmodels.authentication.fragments

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.constants.ServerConstants.FIREBASE_DB_NAME
import com.example.talks.constants.ServerConstants.USER_BIO
import com.example.talks.constants.ServerConstants.USER_IMAGE_STORAGE_PATH
import com.example.talks.constants.ServerConstants.USER_IMAGE_URL
import com.example.talks.constants.ServerConstants.USER_NAME
import com.example.talks.constants.ServerConstants.USER_PHONE_NUMBER
import com.example.talks.constants.ServerConstants.USER_UNIQUE_ID
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

            Firebase.database.getReference(FIREBASE_DB_NAME).get()
                .addOnSuccessListener {

                    for (data in it.children) {

                        val contactNumber =
                            data.child(USER_PHONE_NUMBER).value.toString()
                        val contactUserName =
                            data.child(USER_NAME).value.toString()
                        val contactImageUrl =
                            data.child(USER_IMAGE_URL).value.toString()
                        val contactBio = data.child(USER_BIO).value.toString()
                        val contactId =
                            data.child(USER_UNIQUE_ID).value.toString()

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
            val dbRef = Firebase.database.getReference(FIREBASE_DB_NAME)
            if (userUid != null) {
                dbRef.child(userUid).get().addOnSuccessListener {
                    val userName = it.child(USER_NAME).value.toString()
                    val userImage = it.child(USER_IMAGE_URL).value.toString()
                    val userBio = it.child(USER_BIO).value.toString()

                    val user = hashMapOf(
                        USER_NAME to userName,
                        USER_IMAGE_URL to userImage,
                        USER_BIO to userBio
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
            val dbRef = Firebase.database.getReference(FIREBASE_DB_NAME)
            if (user[USER_UNIQUE_ID] != null) {
                dbRef.child(user[USER_UNIQUE_ID]!!).setValue(user)
                    .addOnSuccessListener {
                        val localUser = hashMapOf(
                            USER_PHONE_NUMBER to user[USER_PHONE_NUMBER],
                            USER_NAME to user[USER_NAME],
                            USER_IMAGE_URL to user[USER_IMAGE_URL],
                            USER_BIO to user[USER_BIO],
                            USER_UNIQUE_ID to user[USER_UNIQUE_ID]
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
                val reference = storageRef.getReference(userId).child(USER_IMAGE_STORAGE_PATH)
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