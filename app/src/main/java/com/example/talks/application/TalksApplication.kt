package com.example.talks.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.talks.others.fileManager.TalksStorageManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime

@HiltAndroidApp
class TalksApplication : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("application starts ===", LocalDateTime.now().toString())
//        FirebaseApp.initializeApp(this)
//        val auth = FirebaseAuth.getInstance()
//        val userID = auth.currentUser?.uid.toString()

        /////////////////////////////////////////////////////////////////////
//        Firebase.database.setPersistenceEnabled(true)
//        val dbRef = Firebase.database.getReference("talks_database_chats")
//        dbRef.keepSynced(false)
//        EmojiManager.install(GoogleEmojiProvider())


        // create directories
//        TalksStorageManager.createDirectoryInPrivateStorage(this)
//        TalksStorageManager.createDirectoryInPublicStorage()
        Log.d("application starts oncreate end ===", LocalDateTime.now().toString())
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