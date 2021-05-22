package com.example.talks.calling

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.calling.peerjs.JavascriptInterface
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityCallingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_calling.*
import kotlinx.coroutines.launch

class CallingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallingBinding

    private lateinit var viewModel: CallActivityViewModel
    private lateinit var databaseViewModel: TalksViewModel

    private var callAction: Int? = null

    private val currentUserID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CallActivityViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)
        setClickListeners()

        val intent = intent
        val phoneNumber = intent.getStringExtra("phoneNumber")
        callAction = intent.getIntExtra("callAction", 0)

        when (callAction) {
            1 -> {
                // sending a call
                if (phoneNumber != null) {
                    setupWebView()
                    displayUserData(phoneNumber)
                }
            }
            2 -> {
                // receiving a call
                setupWebView()
                hideTopLayout()

            }
        }

        viewModel.receiverConnectionID.observe(this, {
            if (it != null) {
                if (it == "call_rejected") {
                    Toast.makeText(this, "call rejected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    startCall(it)
                    hideTopLayout()
                }
            }
        })

    }

    private fun setClickListeners() {
        var videoFeedDisabled = false
        var audioFeedDisabled = false

        callActivityToolbar.setNavigationOnClickListener {
            this.enterPictureInPictureMode()
        }

        audioFeedController.setOnClickListener {
            audioFeedDisabled = if (audioFeedDisabled) {
                webView.evaluateJavascript("javascript:toggleAudio(false)", null)
                audioFeedController.setImageResource(R.drawable.mic_off)
                false
            } else {
                webView.evaluateJavascript("javascript:toggleAudio(true)", null)
                audioFeedController.setImageResource(R.drawable.mic_on)
                true
            }
        }

        videoFeedController.setOnClickListener {
            videoFeedDisabled = if (videoFeedDisabled) {
                webView.evaluateJavascript("javascript:toggleVideo(false)", null)
                videoFeedController.setImageResource(R.drawable.video_off)
                false
            } else {
                webView.evaluateJavascript("javascript:toggleVideo(true)", null)
                videoFeedController.setImageResource(R.drawable.video_on)
                true
            }
        }

        cutCallButton.setOnClickListener {
            webView.destroy()
            finish()
        }
    }

    private fun hideTopLayout() {
        callActivityUserName.visibility = View.INVISIBLE
        callActivityUserImage.visibility = View.INVISIBLE
    }

    private fun startCall(peerID: String?) {
        webView.apply {
            post {
                evaluateJavascript("javascript:startCall(\"$peerID\")", null)
            }
        }
    }

    private fun displayUserData(phoneNumber: String) {
        lifecycleScope.launch {
            databaseViewModel.readSingleContact(phoneNumber).observe(this@CallingActivity, {
                Glide.with(this@CallingActivity).load(it.contactImageUrl).diskCacheStrategy(
                    DiskCacheStrategy.AUTOMATIC
                ).placeholder(R.drawable.ic_baseline_person_color).into(callActivityUserImage)
                callActivityUserName.text = it.contactName

                viewModel.updatePeerConnectionID(currentUserID, it.uId)
            })
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.domStorageEnabled = true
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(
                JavascriptInterface(
                    this@CallingActivity,
                    currentUserID,
                    viewModel,
                ), "Android"
            )

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }
            }
            loadWebPage()
        }
    }

    private fun loadWebPage() {
        val filePath = "file:android_asset/call.html"
        binding.webView.apply {
            loadUrl(filePath)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    when (callAction) {
                        1 -> {
                            initializePeer(1)
                        }
                        2 -> {
                            initializePeer(2)
                        }
                    }
                }
            }
        }
    }

    private fun initializePeer(peerID: Int) {
        binding.webView.post {
            binding.webView.evaluateJavascript("javascript:init($peerID)", null)
        }
    }

    override fun onBackPressed() {
        this.enterPictureInPictureMode()
    }

    override fun onUserLeaveHint() {
        this.enterPictureInPictureMode()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (isInPictureInPictureMode) {
            callActivityToolbar.visibility = View.GONE
            callingControls.visibility = View.GONE
            webView.evaluateJavascript("javascript:setLocalVideoVisibility(false)", null)
        } else {
            callActivityToolbar.visibility = View.VISIBLE
            callingControls.visibility = View.VISIBLE
            webView.evaluateJavascript("javascript:setLocalVideoVisibility(true)", null)
        }
    }
}