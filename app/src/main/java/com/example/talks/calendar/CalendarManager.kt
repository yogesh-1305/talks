package com.example.talks.calendar

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class CalendarManager {

    companion object {

        @SuppressLint("SimpleDateFormat")
        fun getDate(): String {
            val today = Date()
            val format = SimpleDateFormat("yyyymmdd")
            return format.format(today)
        }

    }

}