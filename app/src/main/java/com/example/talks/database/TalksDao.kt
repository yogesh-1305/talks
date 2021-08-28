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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createChatChannel(chatListItem: ChatListItem)


    // Fetch
    @Query("SELECT * FROM user_data ORDER BY id ASC")
    fun readUserData(): LiveData<List<User>>

    @Query("SELECT * FROM talks_contacts WHERE contact_number = :phoneNumber")
    fun readSingleContact(phoneNumber: String): LiveData<TalksContact>

    @Query("SELECT * FROM talks_contacts WHERE isTalksUser = '1' ORDER BY contactName ASC")
    fun readContacts(): LiveData<List<TalksContact>>

    @Query("SELECT contact_number FROM talks_contacts ORDER BY contactName ASC")
    fun readContactPhoneNumbers(): LiveData<List<String>>

    @Query("SELECT * FROM chat_list ORDER by sortTimestamp DESC")
    fun readChatList(): LiveData<List<ChatListItem>>

    @Query("SELECT * FROM talks_messages WHERE chatId = :chatID")
    fun readMessages(chatID: String): LiveData<List<Message>>

    @Query("SELECT DISTINCT chatId FROM talks_messages")
    fun getDistinctMessages(): LiveData<List<String>>

    @Query("SELECT * from talks_messages ORDER by id DESC LIMIT 1")
    fun getLastAddedMessage(): LiveData<Message>

    @Query("SELECT contactNumber FROM chat_list")
    fun getChatChannels(): LiveData<List<String>>

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
    @Query("UPDATE user_data SET bio = :userBio")
    suspend fun updateUserBio(userBio: String)

    @Query("UPDATE chat_list SET contactName = (SELECT contactName from talks_contacts WHERE contactNumber = :contactNumber), chatListImageUrl = (SELECT contactImageUrl from talks_contacts WHERE contactNumber = :contactNumber) WHERE contactNumber = :contactNumber")
    suspend fun updateChatChannelUserName(contactNumber: String)

    @Query("UPDATE chat_list SET messageText = :messageText, sortTimeStamp = :sortTimestamp, messageType = :messageType WHERE contactNumber = :contactNumber")
    suspend fun updateChatChannel(
        messageText: String,
        sortTimestamp: String,
        messageType: String,
        contactNumber: String
    )
}