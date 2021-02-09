package com.example.talks.modal

class FirebaseUser() {

    private var countryName: String = ""
    private var userCountryCode: String = ""
    private var userPhoneNumber: String = ""
    private var userName: String = ""
    private var userEmail: String = ""
    private var userProfileImage: String = ""
    private var isActive: Boolean = true

    constructor(
        userCountryName: String,
        userCountryCode: String,
        userPhoneNumber: String,
        userName: String,
        userEmail: String,
        userProfileImage: String,
        isActive: Boolean
    ) : this() {
        this.countryName = userCountryName
        this.userCountryCode = userCountryCode
        this.userPhoneNumber = userPhoneNumber
        this.userName = userName
        this.userEmail = userEmail
        this.userProfileImage = userProfileImage
        this.isActive = isActive
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

    fun isActive(): Boolean{
        return this.isActive
    }

    fun getCountryCode(): String{
        return this.userCountryCode
    }

    fun getCountryName(): String{
        return this.countryName
    }

}