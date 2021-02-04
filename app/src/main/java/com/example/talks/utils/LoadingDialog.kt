package com.example.talks.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.example.talks.R

class LoadingDialog internal constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null
    fun startDialog() {
        val builder = AlertDialog.Builder(
            activity
        )
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog, null))
        builder.setCancelable(true)
        dialog = builder.create()
        dialog!!.show()
    }

    fun dismiss() {
        dialog!!.dismiss()
    }
}