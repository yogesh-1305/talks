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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ThirdFragmentViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore
    private var storageRef = FirebaseStorage.getInstance()

    val existingUserData: MutableLiveData<TalksContact> by lazy {
        MutableLiveData<TalksContact>()
    }
    val profileImageUrl: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    fun getUserFromDatabase(userUid: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseName = fireStore.collection("user_database")
            val users = databaseName.document("$userUid")
            users.get()
                .addOnSuccessListener {
                    val user = it.toObject<TalksContact>()
                    if (user != null) {
                        existingUserData.value = user

                    } else {
                        existingUserData.value = null

                    }
                }.addOnFailureListener {
                    Log.i("failure check===", it.message.toString())
                }
        }
    }

    fun addUserToFirebaseFireStore(
        user: TalksContact,
        userUid: String?,
        talksViewModel: TalksViewModel
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseName = fireStore.collection("user_database")
            val users = databaseName.document("$userUid")
            users.set(user)
                .addOnSuccessListener {
                    Log.i("success login====", "------------")
                    val localUser = User(
                        0,
                        user.contactNumber,
                        "${user.contactName}",
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
            if (!it.isSuccessful){
                it.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener{
            if (it.isSuccessful){
                profileImageUrl.value = it.result.toString()
            }else{
                profileImageUrl.value = it.exception?.message.toString()
            }
        }
    }
}