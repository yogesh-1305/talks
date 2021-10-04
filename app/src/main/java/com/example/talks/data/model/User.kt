package com.example.talks.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class User(
    val phoneNumber: String?,
    val userName: String?,
    val profileImageUrl: String?,
    val bio: String?,
    val firebaseAuthUID: String?
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var imageLocalPath: String? = null
}