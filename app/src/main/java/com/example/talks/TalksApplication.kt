package com.example.talks

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

class TalksApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
        val dbRef = Firebase.database.getReference("chat_database")
        dbRef.keepSynced(false)

        EmojiManager.install(GoogleEmojiProvider())
    }
}