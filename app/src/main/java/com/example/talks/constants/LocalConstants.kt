package com.example.talks.constants

object LocalConstants {

    const val DATABASE_NAME = "talks_database"

    const val SHARED_PREFERENCES_NAME = "talks_prefs"

    const val KEY_AUTH_STATE = "KEY_AUTH_STATE"
    const val AUTH_STATE_ADD_NUMBER = 1
    const val AUTH_STATE_ADD_OTP = 2
    const val AUTH_STATE_ADD_DATA = 3
    const val AUTH_STATE_FINAL_SETUP = 4
    const val AUTH_STATE_COMPLETE = 5

    const val MEDIA_MIME_TYPE_TEXT = "/text"
    const val MEDIA_MIME_TYPE_IMAGE = "/image"
    const val MEDIA_MIME_TYPE_VIDEO = "/video"
    const val MEDIA_MIME_TYPE_AUDIO = "/audio"

    const val MESSAGE_PENDING = "pending"
    const val MESSAGE_SENT = "sent"
    const val MESSAGE_DELIVERED = "delivered"
    const val MESSAGE_SEEN = "seen"
    const val MESSAGE_RECEIVED = "received"
}