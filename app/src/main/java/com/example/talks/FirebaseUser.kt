package com.example.talks

class FirebaseUser() {
    private var userPhoneNumber: String = ""
    private var userName: String = ""
    private var userEmail: String = ""
    private var userProfileImage: String = ""

    constructor(
        userPhoneNumber: String,
        userName: String,
        userEmail: String,
        userProfileImage: String
    ) : this() {
        this.userPhoneNumber = userPhoneNumber
        this.userName = userName
        this.userEmail = userEmail
        this.userProfileImage = userProfileImage
    }

    fun getUserName(): String {
        return this.userName
    }

    fun getUserPhoneNumber(): String {
        return this.userPhoneNumber
    }

    fun getUserEmail(): String {
        return this.userEmail
    }

    fun getUserProfileImage(): String {
        return this.userProfileImage
    }

}