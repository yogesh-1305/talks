package com.example.talks.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class User(
    val phoneNumber: String?,
    val userName: String?,
    val profileImage: Bitmap?,
    val bio: String?,
    val firebaseAuthUID: String?
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}