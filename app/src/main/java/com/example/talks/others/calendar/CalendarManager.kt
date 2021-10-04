package com.example.talks.others.calendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CalendarManager {

    companion object {

        fun getCurrentDateTime(): String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return current.format(formatter)
        }

        fun getYesterdayDatDate(): String{
            val current = LocalDateTime.now().minusDays(1)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return current.format(formatter)
        }

        fun getTodayDate(): String{
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return current.format(formatter)
        }

        fun getDateForImage(): String{
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            return current.format(formatter)
        }

    }

}