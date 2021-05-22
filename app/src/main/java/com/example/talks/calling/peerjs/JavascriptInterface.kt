package com.example.talks.calling.peerjs

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.example.talks.calling.CallActivityViewModel

class JavascriptInterface(
    val context: Context,
    val currentUserID: String?,
    val viewModel: CallActivityViewModel,
) {

    @JavascriptInterface
    public fun onSenderPeerConnected(peerID: String?) {
        Toast.makeText(context, "connected $peerID", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    public fun onReceiverPeerConnected(peerID: String?) {
        if (peerID != null) {
            viewModel.setMyConnectionID(currentUserID, peerID)
        }
    }
}