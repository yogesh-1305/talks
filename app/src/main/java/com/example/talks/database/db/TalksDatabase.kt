package com.example.talks.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.talks.data.model.ChatListItem
import com.example.talks.data.model.Message
import com.example.talks.data.model.TalksContact
import com.example.talks.data.model.User
import com.example.talks.database.dao.TalksDao
import com.example.talks.database.typeconverters.Converters

@Database(
    entities = [User::class, TalksContact::class, ChatListItem::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TalksDatabase : RoomDatabase() {
    abstract fun talksDao(): TalksDao
}