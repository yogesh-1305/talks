package com.example.talks.data.viewmodels.call.activity

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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CallActivityViewModel : ViewModel() {

    fun setMyConnectionID(currentUserID: String?, connectionID: String) {
        Log.i("rec curr id +++", currentUserID.toString())
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
        destinationUserID: String?,
        currentUserPhoneNumber: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (destinationUserID != null) {
                val data = HashMap<String, Any>()
                data["callerID"] = currentUserID.toString()
                data["callerPhoneNumber"] = currentUserPhoneNumber.toString()
                data["receiverConnectionID"] = ""
                data["rejected"] = false
                Firebase.database.getReference("talks_database").child(destinationUserID)
                    .child("call_stats").setValue(data).addOnCompleteListener {
                        if (it.isComplete) {
                            listenForReceiverConnectionID(destinationUserID)
                        }
                    }
            }
        }
    }

    val receiverConnectionID: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    private fun listenForReceiverConnectionID(destinationUserID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.database.getReference("talks_database").child(destinationUserID)
                .child("call_stats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val peerID = snapshot.child("receiverConnectionID").value.toString()
                        val isCallRejected = snapshot.child("rejected").value as Boolean?

                        if (isCallRejected == true) {
                            receiverConnectionID.value = "call_rejected"
                            return
                        }
                        if (isCallRejected == null){
                            receiverConnectionID.value = null
                            return
                        }
                        receiverConnectionID.value = peerID
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }

    fun cutCallFromCallerSide(receiverID: String){
        val map = HashMap<String, Any?>()
        map["callerID"] = null
        map["callerPhoneNumber"] = null
        map["receiverConnectionID"] = null
        map["rejected"] = null

        GlobalScope.launch(Dispatchers.IO) {
            Firebase.database.getReference("talks_database").child(receiverID)
                .child("call_stats")
                .setValue(map)
        }
    }

}