package com.example.talks.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "talks_contacts")
data class TalksContact (
    @PrimaryKey(autoGenerate = false)
    val number: String,
    val userName: String,
    val imageUrl: String,
    val uId: String,
)