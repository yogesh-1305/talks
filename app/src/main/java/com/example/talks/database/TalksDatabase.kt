package com.example.talks.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, TalksContact::class, ChatListItem::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TalksDatabase : RoomDatabase() {
    abstract fun talksDao(): TalksDao
}