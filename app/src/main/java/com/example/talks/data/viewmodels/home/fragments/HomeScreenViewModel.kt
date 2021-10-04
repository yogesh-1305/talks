package com.example.talks.data.viewmodels.home.fragments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel() {

    val listOfChatItems: MutableLiveData<List<String?>> by lazy {
        MutableLiveData<List<String?>>()
    }

    fun readChatListFromFireStore(currentUserId: String?, databaseViewModel: TalksViewModel) {
        val list = ArrayList<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserId != null) {
                val dbRef = Firebase.database.getReference("talks_database").child(currentUserId)
                    .child("chats")
                dbRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        Log.i("snapshot***", snapshot.children.toString())
                        val children = snapshot.children
                        for (data in children) {
                            val chatKeys = snapshot.key
                            list.add(chatKeys)
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.i("child changed***", snapshot.key.toString())
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        }
    }
}