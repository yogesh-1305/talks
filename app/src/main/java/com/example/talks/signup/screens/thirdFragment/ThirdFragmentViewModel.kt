package com.example.talks.signup.screens.thirdFragment

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.User
import com.example.talks.database.UserViewModel
import com.example.talks.modal.ServerUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class ThirdFragmentViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore

    val existingUserData: MutableLiveData<ServerUser> by lazy {
        MutableLiveData<ServerUser>()
    }

    fun getUserFromDatabase(userUid: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseName = fireStore.collection("user_database")
            val users = databaseName.document("$userUid")
            users.get()
                .addOnSuccessListener {
                    val user = it.toObject<ServerUser>()
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
        user: ServerUser?,
        userUid: String?,
        userViewModel: UserViewModel
    ) {
        if (user != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val databaseName = fireStore.collection("user_database")
                val users = databaseName.document("$userUid")
                users.set(user)
                    .addOnSuccessListener {
                        Log.i("success login====", "------------")
                        val localUser = User(
                            0,
                            user.getUserPhoneNumber(),
                            user.getUserName(),
                            user.getUserProfileImage()
                        )
                        addUserToLocalDatabase(localUser, userViewModel)

                    }.addOnFailureListener {
                        Log.i("failed login====", it.toString())
                    }
            }
        } else {
            Log.i("user null===", "$user")
        }
    }

    private fun addUserToLocalDatabase(user: User, userViewModel: UserViewModel) {
        Log.i("database-------", user.phoneNumber)
        userViewModel.addUser(user)
    }

    fun readLocalUserData(userViewModel: UserViewModel): LiveData<List<User>> {
        return userViewModel.readAllUserData
    }

    fun convertImageToBase64(image: Bitmap?): String? {
        return if (image != null) {
            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val bytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
            base64String
        } else {
            null
        }
    }
}