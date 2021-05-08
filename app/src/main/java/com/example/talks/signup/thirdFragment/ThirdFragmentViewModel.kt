package com.example.talks.signup.thirdFragment

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.database.User
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ThirdFragmentViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore
    private var storageRef = FirebaseStorage.getInstance()

    val existingUserData: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
    val profileImageUrl: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    fun getUserFromDatabase(userUid: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database")
            if (userUid != null) {
                dbRef.child(userUid).get().addOnSuccessListener {
                    val userName = it.child("contactUserName").value.toString()
                    val userImage = it.child("contactImageUrl").value.toString()
                    val userBio = it.child("contact_bio").value.toString()

                    val user = User(
                        0, "",
                        userName, userImage, "", userBio, ""
                    )
                    existingUserData.value = user
                }
            }
        }
    }

    fun addUserToFirebaseDatabase(
        user: TalksContact,
        userUid: String?,
        talksViewModel: TalksViewModel
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database")
            if (userUid != null) {
                dbRef.child(userUid).setValue(user).addOnSuccessListener {
                    Log.i("user Added to db===", user.contactNumber)
                    val localUser = User(
                        0,
                        user.contactNumber,
                        "${user.contactUserName}",
                        user.contactImageUrl,
                        user.contactImageBitmap,
                        user.contact_bio,
                        user.uId
                    )
                    addUserToLocalDatabase(localUser, talksViewModel)
                }.addOnFailureListener {
                    Log.i("failed login====", it.toString())
                }
            }
        }
    }

    private fun addUserToLocalDatabase(user: User, talksViewModel: TalksViewModel) {
        Log.i("database-------", user.phoneNumber.toString())
        talksViewModel.addUser(user)
    }

    fun readLocalUserData(talksViewModel: TalksViewModel): LiveData<List<User>> {
        return talksViewModel.readAllUserData
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