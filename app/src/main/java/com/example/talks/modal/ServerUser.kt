package com.example.talks.modal

class ServerUser() {

    private var countryName: String = ""
    private var userCountryCode: String = ""
    private var userPhoneNumber: String = ""
    private var userName: String = ""
    private var userProfileImage: String = ""
    private var isActive: Boolean = true
    private var uid: String? = ""

    constructor(
        userCountryName: String,
        userCountryCode: String,
        userPhoneNumber: String,
        userName: String,
        userProfileImage: String,
        isActive: Boolean,
        uid: String?
    ) : this() {
        this.countryName = userCountryName
        this.userCountryCode = userCountryCode
        this.userPhoneNumber = userPhoneNumber
        this.userName = userName
        this.userProfileImage = userProfileImage
        this.isActive = isActive
        this.uid = uid
    }

    fun getUserName(): String {
        return this.userName
    }
    fun getUserPhoneNumber(): String {
        return this.userPhoneNumber
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
    fun getUid(): String?{
        return this.uid
    }

}