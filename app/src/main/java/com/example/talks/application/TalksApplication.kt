package com.example.talks.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.talks.fileManager.TalksStorageManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TalksApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser?.uid.toString()

        /////////////////////////////////////////////////////////////////////
        Firebase.database.setPersistenceEnabled(true)
        val dbRef = Firebase.database.getReference("talks_database_chats")
        dbRef.keepSynced(false)
        EmojiManager.install(GoogleEmojiProvider())


        // create directories
        TalksStorageManager.createDirectoryInPrivateStorage(this)
        TalksStorageManager.createDirectoryInPublicStorage()
    }

    private fun createNotificationChannelForCalls() {
        val channelId = "Calls"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create notification channel
            val name = "Call Notification"
            val desc = "Make sound when getting a call"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = desc
            mChannel.enableVibration(true)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}