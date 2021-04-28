package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val phoneNumber: String,
    val userName: String,
    val profileImage: String,
    val userBio: String,
    val userId: String
)