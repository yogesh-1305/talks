package com.example.talks.profile.editingScreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileEditViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore

    val isUserUpdated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val isBioUpdated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun setUsername(name: String, uid: String, userDatabase: UserViewModel) {
        isUserUpdated.value = false
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("user_database")
                .document(uid).update("userName", name)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("name===", "updated")
                        userDatabase.updateUserName(name)
                        isUserUpdated.value = true
                    }
                }
        }
    }

    fun setBio(bio: String, uid: String, userDatabase: UserViewModel) {
        isBioUpdated.value = false
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("user_database")
                .document(uid).update("userBio", bio)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("bio===", "updated")
                        userDatabase.updateUserBio(bio)
                        isBioUpdated.value = true
                    } else {
                        isBioUpdated.value = false
                    }
                }
        }
    }

}