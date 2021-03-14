package com.example.talks.home.homeScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeScreenViewModel : ViewModel() {
    private var fireStore: FirebaseFirestore = Firebase.firestore

    fun readChatListFromFireStore(currentUserId: String?){
        if (currentUserId != null) {
            fireStore.collection("chat_database").document(currentUserId).get()
                .addOnSuccessListener {
                    
                }
        }
    }
}