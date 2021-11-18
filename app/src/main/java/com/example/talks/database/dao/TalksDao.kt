package com.example.talks.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.talks.data.model.*

@Dao
interface TalksDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContact(contact: TalksContact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createChatChannel(chatListItem: ChatListItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createChatChannels(list: List<ChatListItem>)

    // Fetch
    @Query("SELECT * FROM user_data ORDER BY id ASC")
    fun readUserData(): LiveData<List<User>>

    @Query("SELECT * FROM talks_contacts WHERE contact_number = :userPhoneNumber")
    fun readSingleContact(userPhoneNumber: String): LiveData<TalksContact>

    @Query("SELECT * FROM talks_contacts WHERE isTalksUser = '1' ORDER BY contactName ASC")
    fun readContacts(): LiveData<List<TalksContact>>

    @Query("SELECT contact_number FROM talks_contacts ORDER BY contactName ASC")
    fun readContactPhoneNumbers(): LiveData<List<String>>

    @Query("""
            select uId as contact_id, chat_list.contact_number, talks_contacts.contactName, contactImageUrl, 
            messageText, messageType, creation_time, sentByMe, isChatPinned, isChatArchived, 
            isChatMuted, unseenMessagesCount, lastReadMessageID
            from chat_list 
            LEFT JOIN talks_contacts on chat_list.contact_number = talks_contacts.contact_number
            LEFT JOIN talks_messages on chat_list.latest_message_id = talks_messages.id
            ORDER by creation_time desc
        """
    )
    fun readHomeScreenChannelList(): LiveData<List<HomeScreenChannelList>>

    @Query("SELECT * FROM talks_messages WHERE chatId = :chatID")
    fun readMessages(chatID: String): LiveData<List<Message>>

    @Query("SELECT DISTINCT chatId FROM talks_messages")
    fun getDistinctMessages(): LiveData<List<String>>

    @Query("SELECT * from talks_messages ORDER by id DESC LIMIT 1")
    fun getLastAddedMessage(): LiveData<Message>

    @Query("select creation_time from talks_messages order by creation_time desc limit 1")
    suspend fun getLastMessageCreationTime(): String

    @Query("SELECT contact_number FROM chat_list")
    fun getChatListPhoneNumbers(): LiveData<List<String>>

    @Query("select distinct chatID from talks_messages")
    fun getDistinctPhoneNumbers(): LiveData<List<String>>

    @Query("""insert into chat_list (contact_number, latest_message_id) 
        select chatID as contact_number, id as latest_message_id 
        from talks_messages 
        GROUP by chatID 
        order by latest_message_id""")
    suspend fun getMessagesDataForChatList()

    // Update

    // user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(contact: TalksContact)

    // user name
    @Query("UPDATE user_data SET userName = :userName")
    suspend fun updateUserName(userName: String)

    // user image
    @Query("UPDATE user_data SET profileImageUrl = :userImage, imageLocalPath = :imageLocalPath")
    suspend fun updateUserImage(userImage: String, imageLocalPath: String)

    // user bio
    @Query("UPDATE user_data SET bio = :userBio")
    suspend fun updateUserBio(userBio: String)

    @Query("UPDATE chat_list SET latest_message_id = :latestMessageId where contact_number = :contact_number")
    suspend fun updateChatListLatestMessage(
        contact_number: String,
        latestMessageId: Int,
    )

    @Query("UPDATE talks_messages SET status = :status WHERE creation_time = :creationTime")
    suspend fun updateMessageStatus(status: String, creationTime: String)
}