package com.example.talks.calling

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallActivityViewModel : ViewModel() {

    fun setMyConnectionID(currentUserID: String?, connectionID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUserID != null) {
                Firebase.database.getReference("talks_database").child(currentUserID)
                    .child("call_stats").child("receiverConnectionID")
                    .setValue(connectionID).addOnCompleteListener {
                        if (it.isComplete) {
                            Log.i("rec conn id +++", connectionID)
                        }
                    }
            }
        }
    }

    fun updatePeerConnectionID(
        currentUserID: String?,
        destinationUserID: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (destinationUserID != null) {
                val data = HashMap<String, Any>()
                data["callerID"] = currentUserID.toString()
                data["receiverConnectionID"] = ""
                data["rejected"] = false
                Firebase.database.getReference("talks_database").child(destinationUserID)
                    .child("call_stats").setValue(data).addOnCompleteListener {
                        if (it.isComplete) {
                            Log.i("connection ID+++", currentUserID.toString())
                            listenForReceiverConnectionID(destinationUserID)
                        }
                    }
            }
        }
    }

    val receiverConnectionID: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private fun listenForReceiverConnectionID(destinationUserID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.database.getReference("talks_database").child(destinationUserID)
                .child("call_stats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val peerID = snapshot.child("receiverConnectionID").value.toString()
                        val isCallRejected = snapshot.child("rejected").value

                        if (isCallRejected == true) {
                            receiverConnectionID.value = "call_rejected"
                            return
                        }
                        receiverConnectionID.value = peerID
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }

}