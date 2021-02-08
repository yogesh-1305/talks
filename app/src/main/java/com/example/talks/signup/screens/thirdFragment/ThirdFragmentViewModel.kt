package com.example.talks.signup.screens.thirdFragment

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.FirebaseUser
import com.example.talks.database.User
import com.example.talks.database.UserViewModel
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

    val existingUserData: MutableLiveData<FirebaseUser> by lazy {
        MutableLiveData<FirebaseUser>()
    }
    val userCreatedInFireStore: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val userCreatedInRoomDatabase: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val profileImageUrl: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    fun getUserFromDatabase(phoneNumber: String) {

        fireStore.collection("user_data").document(phoneNumber)
            .get()
            .addOnSuccessListener {
                val user = it.toObject<FirebaseUser>()
                if (user != null) {
                    // ask to update details of existing user
//                    userAlreadyExist.value = true
                    retrieveDetailsOfExistingUser(user)
                }
//                else {
//                    // ask to enter details for new user and create new user
////                    userAlreadyExist.value = false
//                }
            }.addOnFailureListener {
                Log.i("failure check===", it.message.toString())
            }

    }

    fun addUserToLocalDatabase(user: User, userViewModel: UserViewModel) {
        Log.i("database-------", user.phoneNumber)
        userViewModel.addUser(user)
        userCreatedInRoomDatabase.value = true
    }

    fun addUserToFirebaseFireStore(user: FirebaseUser?) {
        if (user != null) {
            fireStore.collection("user_data").document(user.getUserPhoneNumber())
                .set(user)
                .addOnSuccessListener {
                    Log.i("success login====", "------------")
                    userCreatedInFireStore.value = true
                }.addOnFailureListener {
                    Log.i("failed login====", it.toString())
                    userCreatedInFireStore.value = false
                }
        }
    }

    private fun retrieveDetailsOfExistingUser(user: FirebaseUser) {
        existingUserData.value = user
    }

    fun uploadImageToStorage(image: Uri?, phoneNumber: String?) {
        if (image!= null) {
            viewModelScope.launch(Dispatchers.IO) {
                val storageRef = FirebaseStorage.getInstance().getReference("User_Profile_Images")
                val userImages = storageRef.child(phoneNumber!!).child("Profile")
                val uploadTask = userImages.putFile(image)
                uploadTask.addOnSuccessListener {
                    getDownloadUrl(uploadTask, userImages)
                }
            }
        }else{
            profileImageUrl.value = null
        }

    }

    private fun getDownloadUrl(uploadTask: UploadTask, userImage: StorageReference) {
        uploadTask.continueWithTask{ it ->
            if (!it.isSuccessful){
                it.exception?.let {
                    throw it
                }
            }
            userImage.downloadUrl
        }.addOnCompleteListener{
            if (it.isSuccessful){
                profileImageUrl.value = it.result.toString()
            }else{
                Log.i("failure upload", it.toString())
            }
        }
    }
}