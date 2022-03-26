package com.example.talks.session

import android.content.SharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Array.get
import javax.inject.Inject

object SessionManager {
    var prefs: SharedPreferences? = null

    fun setSessionInstance(sharedPreferences: SharedPreferences) {
        if (prefs == null) {
            prefs = sharedPreferences
        }
    }

    var fcmToken: String?
        get() = prefs?.getString("fcm_token", "")
        set(value) = prefs?.edit()?.putString("fcm_token", value)!!.apply()
}