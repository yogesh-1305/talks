package com.example.talks.home.profileScreen.editingScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.UserDatabase
import com.example.talks.database.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileEditViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore

    fun setUsername(name: String, uid: String, userDatabase: UserViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("user_database")
                .document(uid).update("userName", name)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("name===", "updated")
                        userDatabase.updateUserName(name)
                    }
                }
        }
    }

    fun setBio(name: String) {

    }

}