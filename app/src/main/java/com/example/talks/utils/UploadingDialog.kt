package com.example.talks.utils

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.example.talks.R

class UploadingDialog internal constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null
    @SuppressLint("InflateParams")
    fun startDialog() {
        val builder = AlertDialog.Builder(
            activity
        )
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.uploading_dialog, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

    fun dismiss() {
        dialog!!.dismiss()
    }
}