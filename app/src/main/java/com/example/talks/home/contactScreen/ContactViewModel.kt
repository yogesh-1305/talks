package com.example.talks.home.contactScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ContactViewModel : ViewModel() {
    var users : MutableLiveData<Contact> = MutableLiveData()

    fun checkUsers(auth: FirebaseAuth){

    }

}