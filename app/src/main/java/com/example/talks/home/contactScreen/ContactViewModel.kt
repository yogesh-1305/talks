package com.example.talks.home.contactScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.talks.modal.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ContactViewModel : ViewModel() {

    private var fireStore: FirebaseFirestore = Firebase.firestore

    var users : MutableLiveData<List<String>> = MutableLiveData()

    fun checkUsers(){
        val usersList = ArrayList<String>()
        fireStore.collection("user_data")
            .get().addOnSuccessListener {
                for (document in it) {
                    val user = document.toObject<FirebaseUser>().getUserPhoneNumber()
                    if (!usersList.contains(user)){
                        usersList.add(user)
                    }
                    users.value = usersList
                }
            }
    }

}