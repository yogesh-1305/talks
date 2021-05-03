package com.example.talks.modal

class MessageSchema() {

    var text: String = ""
    var time: String = ""
    var date: String = ""
    var uid: String = ""
    var read: Boolean = false
    var sent: Boolean = false

    constructor(
        text: String,
        time: String,
        date: String,
        uid: String,
        read: Boolean,
        sent: Boolean
    ) : this() {
        this.text = text
        this.time = time
        this.date = date
        this.uid = uid
        this.read = read
        this.sent = sent
    }

    @JvmName("getText1")
    private fun getText(): String {
        return this.text
    }
    @JvmName("getTime1")
    private fun getTime(): String {
        return this.time
    }
    @JvmName("getDate1")
    private fun getDate(): String {
        return this.date
    }

    @JvmName("getUid1")
    private fun getUid(): String {
        return this.uid
    }

    @JvmName("getRead1")
    private fun getRead(): Boolean {
        return this.read
    }

    @JvmName("getSent1")
    private fun getSent(): Boolean {
        return this.sent
    }
}