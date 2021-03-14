package com.example.talks.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.sql.Blob

@Dao
interface UserDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContact(contact: TalksContact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addChatListItem(chatListItem: ChatListItem)



    // Fetch
    @Query("SELECT * FROM user_data ORDER BY id ASC")
    fun readUserData() : LiveData<List<User>>

    @Query("SELECT * FROM talks_contacts ORDER BY userName ASC")
    fun readContacts(): LiveData<List<TalksContact>>

    @Query("SELECT * FROM chat_list ORDER BY timestamp ASC")
    fun readChatList(): LiveData<List<ChatListItem>>


    // Update
    @Update
    suspend fun updateUser(contact: TalksContact)

    @Update
    suspend fun updateChatListItem(chatListItem: ChatListItem)

}