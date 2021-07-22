package com.example.talks.calling.peerjs

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.example.talks.calling.CallActivityViewModel

class JavascriptInterface(
    val context: Context,
    val currentUserID: String?,
    private val receiverID: String,
    private val currentUSerPhoneNumber: String,
    val viewModel: CallActivityViewModel,
) {

    @JavascriptInterface
    public fun onSenderPeerConnected() {
        viewModel.updatePeerConnectionID(currentUserID, receiverID, currentUSerPhoneNumber)
    }

    @JavascriptInterface
    public fun onReceiverPeerConnected(peerID: String?) {
        if (peerID != null) {
            viewModel.setMyConnectionID(currentUserID, peerID)
            Toast.makeText(context, "peerid $peerID", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    public fun error(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}