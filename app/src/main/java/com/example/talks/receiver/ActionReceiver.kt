package com.example.talks.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.talks.calling.CallingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val action = intent?.getIntExtra("action", 2)
        val peerConnectionID = intent?.getStringExtra("peerConnectionID")
        Toast.makeText(context, "$action", Toast.LENGTH_SHORT).show()

        when (action) {
            0 -> {
                // decline call
                clearNotification(context)
                closeNotificationTray(context)
                declineIncomingCall(currentUserID)
            }
            1 -> {
                // accept call
                clearNotification(context)
                closeNotificationTray(context)
                moveToCallingActivity(context, currentUserID)
            }
            2 -> {
                Toast.makeText(context, "Error Starting Call!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun declineIncomingCall(currentUserID: String) {

        val map = HashMap<String, Any?>()
        map["callerID"] = ""
        map["callerPhoneNumber"] = ""
        map["receiverConnectionID"] = ""
        map["rejected"] = true

        GlobalScope.launch(Dispatchers.IO) {
            Firebase.database.getReference("talks_database").child(currentUserID)
                .child("call_stats")
                .setValue(map)
        }
    }

    private fun moveToCallingActivity(context: Context?, currentUserID: String) {
        val map = HashMap<String, Any?>()
        map["callerID"] = ""
        map["callerPhoneNumber"] = ""
        map["receiverConnectionID"] = ""
        map["rejected"] = false
        GlobalScope.launch(Dispatchers.IO) {

            Firebase.database.getReference("talks_database").child(currentUserID)
                .child("call_stats")
                .setValue(map)
        }
        val callingActivityIntent = Intent(context, CallingActivity::class.java)
        callingActivityIntent.putExtra("callAction", 2)
        callingActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(callingActivityIntent)
    }

    private fun closeNotificationTray(context: Context?) {
//        close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context?.sendBroadcast(it)
    }

    private fun clearNotification(context: Context?) {
//        clear the notification
        context?.apply {
            NotificationManagerCompat.from(this).cancel(1001)
        }
    }
}