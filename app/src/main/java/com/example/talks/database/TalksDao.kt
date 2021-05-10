package com.example.talks.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TalksDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContact(contact: TalksContact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMessage(message: Message)


    // Fetch
    @Query("SELECT * FROM user_data ORDER BY id ASC")
    fun readUserData(): LiveData<List<User>>

    @Query("SELECT * FROM talks_contacts WHERE isTalksUser = '1' ORDER BY contactName ASC")
    fun readContacts(): LiveData<List<TalksContact>>

    @Query("SELECT contactNumber FROM talks_contacts ORDER BY contactName ASC")
    fun readContactPhoneNumbers(): LiveData<List<String>>

    @Query("SELECT * FROM chat_list")
    fun readChatList(): LiveData<List<ChatListItem>>

    @Query("SELECT * FROM talks_messages WHERE chatId = :chatID")
    fun readMessages(chatID: String): LiveData<List<Message>>

    @Query("INSERT or IGNORE INTO chat_list ('contactNumber','contactName','contactImageUrl') SELECT contactNumber, contactName, contactImageUrl FROM talks_contacts WHERE contactNumber = :contactNumber")
    fun createChatChannel(contactNumber: String)

    @Query("SELECT DISTINCT chatId FROM talks_messages")
    fun getDistinctMessages(): LiveData<List<String>>

    @Query("SELECT * from talks_messages ORDER by id DESC LIMIT 1")
    fun getLastAddedMessage(): LiveData<Message>

    // Update

    // user
    @Update
    suspend fun updateUser(contact: TalksContact)

    // user name
    @Query("UPDATE user_data SET userName = :userName")
    suspend fun updateUserName(userName: String)

    // user image
    @Query("UPDATE user_data SET profileImage = :userImage")
    suspend fun updateUserImage(userImage: String)

    // user bio
    @Query("UPDATE user_data SET userBio = :userBio")
    suspend fun updateUserBio(userBio: String)

    @Query("UPDATE chat_list SET latestMessage = (SELECT messageText from talks_messages WHERE chatId = :userID ORDER by id DESC LIMIT 1) WHERE contactNumber = :userID")
    suspend fun updateLastMessageInChatChannel(userID: String)
}