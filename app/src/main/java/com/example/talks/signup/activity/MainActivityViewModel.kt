package com.example.talks.signup.activity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.modal.ServerUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private var fireStore: FirebaseFirestore = Firebase.firestore

    val users: MutableLiveData<MutableList<ServerUser>> by lazy {
        MutableLiveData<MutableList<ServerUser>>()
    }

    fun getUsersFromServer(
        databaseContactList: List<String>,
        contactNameList: HashMap<String, String>,
        databaseViewModel: TalksViewModel
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            fireStore.collection("user_database").get()
                .addOnSuccessListener {
                    Log.i("contacts Snapshot====", it.toString())

                    for (document in it) {

                        val contactNumber = document.getString("contactNumber")
                        val contactUserName = document.getString("contactUserName")
                        val contactImageUrl = document.getString("contactImageUrl")
                        val contactBio = document.getString("contact_bio")
                        val contactStatus = document.getString("status")
                        val contactId = document.getString("uid")
                        val contactImageBitmap = document.getString("contactImageBitmap")

                        Log.i("user List====", databaseContactList.toString())
                        if (databaseContactList.contains(contactNumber!!)) {
                            Log.i("contacts====", contactNumber.toString())
                            val user = TalksContact(
                                contactNumber,
                                true,
                                contactNameList[contactNumber],
                                contactUserName,
                                contactImageUrl,
                                contactImageBitmap,
                                contactId,
                                contactStatus,
                                contactBio
                            )
                            databaseViewModel.updateUser(user)
                        }

                    }

                }
        }
    }
}