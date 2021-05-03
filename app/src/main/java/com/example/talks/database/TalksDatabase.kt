package com.example.talks.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, TalksContact::class, ChatListItem::class, Message::class],
    version = 1,
    exportSchema = false
)
abstract class TalksDatabase : RoomDatabase() {

    abstract fun talksDao(): TalksDao

    companion object {

        @Volatile
        private var INSTANCE: TalksDatabase? = null

        fun getDatabase(context: Context): TalksDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TalksDatabase::class.java,
                    "talks_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}