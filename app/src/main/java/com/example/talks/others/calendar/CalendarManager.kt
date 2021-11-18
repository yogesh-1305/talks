package com.example.talks.others.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class CalendarManager {

    companion object {

        fun getCurrentDateTime(): LocalDateTime? {
            return LocalDateTime.now()
        }

        fun String.parseTime(): String {
            val dtObject = LocalDateTime.parse(this)
            val hours = dtObject.hour
            val formattedHours = if (hours > 12) (hours - 12) else hours

            val minutes =
                if (dtObject.minute.toString().length == 2) dtObject.minute else "0${dtObject.minute}"

            val date = dtObject.toLocalDate().parseDay()

            return when {
                hours > 12 -> {
                    "$date at $formattedHours:$minutes pm"
                }
                hours == 12 -> {
                    "$date at 12:$minutes am"
                }
                hours < 12 -> {
                    "$date at $formattedHours:$minutes am"
                }
                else -> {
                    "N/A"
                }
            }
        }

        private fun LocalDate.parseDay(): String {
            return when {
                this == getTodayDate() -> {
                    "Today"
                }
                this == getYesterdayDate() -> {
                    "Yesterday"
                }
                else -> {
                    val date = this.dayOfMonth
                    val month = this.month.toString().substring(0,3)
                    val year = this.year
                    "$month $date $year"
                }
            }
        }

        private fun getYesterdayDate(): LocalDate? {
            val current = LocalDateTime.now().minusDays(1)
            return current.toLocalDate()
        }

        private fun getTodayDate(): LocalDate? {
            val current = LocalDateTime.now()
            return current.toLocalDate()
        }
    }

}