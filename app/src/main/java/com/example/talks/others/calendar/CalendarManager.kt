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

            val minutes = dtObject.minute

            val date = dtObject.toLocalDate().parseDay()

            return when {
                hours > 12 -> {
                    "$date, $formattedHours:$minutes pm"
                }
                hours == 12 -> {
                    "$date, 12:$minutes am"
                }
                hours < 12 -> {
                    "$date, $formattedHours:$minutes am"
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
                    val month = this.month
                    val year = this.year
                    "$month $date $year"
                }
            }
        }

        fun getYesterdayDate(): LocalDate? {
            val current = LocalDateTime.now().minusDays(1)
            return current.toLocalDate()
        }

        fun getTodayDate(): LocalDate? {
            val current = LocalDateTime.now()
            return current.toLocalDate()
        }
    }

}