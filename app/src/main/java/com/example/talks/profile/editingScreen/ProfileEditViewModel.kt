package com.example.talks.profile.editingScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.database.TalksViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileEditViewModel : ViewModel() {

    val isUserUpdated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val isBioUpdated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun setUsername(name: String, uid: String, talksDatabase: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {

            val dbRef = Firebase.database.getReference("talks_database")
            val userNameUpdate: MutableMap<String, String> = HashMap()
            userNameUpdate["contactUserName"] = name

            dbRef.child(uid).updateChildren(userNameUpdate as Map<String, String>)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        talksDatabase.updateUserName(name)
                        isUserUpdated.value = true
                    }
                }
        }
    }

    fun setBio(bio: String, uid: String, talksDatabase: TalksViewModel) {
        isBioUpdated.value = false
        viewModelScope.launch(Dispatchers.IO) {
            val dbRef = Firebase.database.getReference("talks_database")
            val bioUpdate: MutableMap<String, String> = HashMap()
            bioUpdate["contactUserName"] = bio

            dbRef.child(uid).updateChildren(bioUpdate as Map<String, String>)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        talksDatabase.updateUserBio(bio)
                        isUserUpdated.value = true
                    }
                }
        }
    }

}