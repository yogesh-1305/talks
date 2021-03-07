package com.example.talks.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.sql.Blob

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContact(contact: TalksContact)

    @Query("SELECT * FROM user_data ORDER BY id ASC")
    fun readUserData() : LiveData<List<User>>

    @Query("SELECT * FROM talks_contacts ORDER BY userName ASC")
    fun readContacts(): LiveData<List<TalksContact>>

    @Update
    suspend fun updateUser(contact: TalksContact)

}