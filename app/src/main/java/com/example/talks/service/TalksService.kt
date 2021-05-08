package com.example.talks.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TalksService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        val userId = FirebaseAuth.getInstance().currentUser.phoneNumber
//
//        val dbRef = Firebase.database.getReference("talks_database_chats")
//        if (userId != null) {
//            dbRef.child(userId).addChildEventListener(object : ChildEventListener {
//                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                    Log.i("snapshot+++", snapshot.getValue(DBMessage::class.java).toString())
//                    val file = filesDir
//                    val folder = File(file, "service")
//                    folder.mkdirs()
//
//                }
//
//                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onChildRemoved(snapshot: DataSnapshot) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//
//        }

        return START_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onDestroy()
    }
}