package com.example.talks

import android.app.Application
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class TalksApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.database.setPersistenceEnabled(true)
        val dbRef = Firebase.database.getReference("chat_database")
        dbRef.keepSynced(false)
        Log.i("application log======", "runtime")

        val cal = Calendar.getInstance()
    }
}