package com.example.talks.home.activity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.TalksContact
import com.example.talks.database.UserViewModel
import com.example.talks.modal.ServerUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel : ViewModel() {
    private var fireStore: FirebaseFirestore = Firebase.firestore

    val users: MutableLiveData<List<ServerUser>> by lazy {
        MutableLiveData<List<ServerUser>>()
    }

    fun getUsersFromServer(contacts: List<String>) {

        viewModelScope.launch(Dispatchers.IO) {

            val usersList = ArrayList<ServerUser>()

            fireStore.collection("user_database")
                .get().addOnSuccessListener {

                    for (document in it) {
                        val user = document.toObject<ServerUser>()

                        if (!usersList.contains(user)) {

                            if (contacts.contains(user.getUserPhoneNumber())) {
                                usersList.add(user)
                                Log.i("user phone check===", user.getUserPhoneNumber())
                            }

                        }
                    }

                    users.value = usersList
                }
        }
    }

    fun getCurrentUserData(uid: String, databaseViewModel: UserViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("user_database").document(uid)
                .get().addOnSuccessListener {
                    val currentUser = it.toObject<ServerUser>()
                    if (currentUser != null) {
                        val localUser = TalksContact(currentUser.getUserPhoneNumber(),currentUser.getUserName(), currentUser.getUserProfileImage(),uid)
                        databaseViewModel.updateUser(localUser)
                    }
                }
        }
    }

}