package com.example.talks.database

import androidx.lifecycle.LiveData
import androidx.room.*

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

    // user
    @Update
    suspend fun updateUser(contact: TalksContact)

    // chat list item
    @Update
    suspend fun updateChatListItem(chatListItem: ChatListItem)

    // user name
    @Query("UPDATE user_data SET userName = :userName")
    suspend fun updateUserName(userName: String)

    // user image
    @Query("UPDATE user_data SET profileImage = :userImage")
    suspend fun updateUserImage(userImage: String)

    // user bio
    @Query("UPDATE user_data SET userBio = :userBio")
    suspend fun updateUserBio(userBio: String)

}