package com.example.talks.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllUserData : LiveData<List<User>>
    private val repository : UserRepository
    val readAllContacts : LiveData<List<TalksContact>>

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllUserData = repository.readAllUserData
        readAllContacts = repository.readContacts
    }

    fun addUser(user: User){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun addContact(contact: TalksContact){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addContactList(contact)
        }
    }

}