package com.example.talks.ui.chat.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.others.Helper
import com.example.talks.R
import com.example.talks.others.calendar.CalendarManager
import com.example.talks.ui.call.activity.CallingActivity
import com.example.talks.data.adapters.chat.activity.ChatAdapter
import com.example.talks.data.viewmodels.chat.activity.ChatViewModel
import com.example.talks.data.viewmodels.chat.ChatViewModelFactory
import com.example.talks.data.model.ChatListItem
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.ActivityChatBinding
import com.example.talks.gallery.attachmentsGallery.activity.AttachmentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private val databaseViewModel: TalksViewModel by viewModels()
    private lateinit var viewModelFactory: ChatViewModelFactory
    private val viewModel: ChatViewModel by viewModels() { viewModelFactory }
    private lateinit var binding: ActivityChatBinding

    private lateinit var chatAdapter: ChatAdapter

    private var auth = FirebaseAuth.getInstance()
    private var senderPhoneNumber = auth.currentUser?.phoneNumber
    private var senderIdentity = auth.currentUser?.uid

    private var isTextEmpty = true
    private var messageToBeSent = ""
    private var receiverID = ""


    var userNameSendingToCallingActivity = ""
    var userIDSendingToCallingActivity = ""
    var userImageStringSendingToCallingActivity = ""

    private lateinit var emojiPopup: EmojiPopup

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val phoneNumber = intent.getStringExtra("contactNumber")

        viewModelFactory =
            ChatViewModelFactory(senderPhoneNumber, phoneNumber, databaseViewModel)

        if (phoneNumber != null) {
            observeUserData(phoneNumber)
            receiverID = phoneNumber
        }

        setupRecyclerView()
        lifecycleScope.launch {
            databaseViewModel.readMessages(receiverID)
                .observe(this@ChatActivity, { list ->
                   list?.let {
                       chatAdapter.submitChatList(list)
                   }
                })
        }

        binding.chatActivityBackButton.setOnClickListener {
            finish()
        }

        messageEditText.addTextChangedListener {

            if (it != null) {
                if (it.isEmpty()) {
                    isTextEmpty = true
                    attachButton.visibility = View.VISIBLE
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                } else {
                    messageToBeSent = it.toString()
                    isTextEmpty = false
                    attachButton.visibility = View.GONE
                    micButton.setImageResource(R.drawable.ic_baseline_send_24)
                }
            }
        }

        chatVideoCallButton.setOnClickListener {
            val callingIntent = Intent(this, CallingActivity::class.java)
            callingIntent.putExtra("phoneNumber", phoneNumber)
            callingIntent.putExtra(
                "userNameSendingToCallingActivity",
                userNameSendingToCallingActivity
            )
            callingIntent.putExtra("userIDSendingToCallingActivity", userIDSendingToCallingActivity)
            callingIntent.putExtra(
                "userImageStringSendingToCallingActivity",
                userImageStringSendingToCallingActivity
            )
            callingIntent.putExtra("callAction", 1)
            startActivity(callingIntent)
        }

        micButton.setOnClickListener {
            when {
                isTextEmpty -> {
                    Toast.makeText(this, "mic process", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.messageEditText.text = null
                    val time = CalendarManager.getCurrentDateTime()
                    viewModel.sendMessage(messageToBeSent, time)
                }
            }
        }

        var emojiIconShown = true
        emojiPopup = EmojiPopup.Builder.fromRootView(chatActivityRootView).build(messageEditText)
        emojiButton.setOnClickListener {
            emojiPopup.toggle()
            emojiIconShown = if (emojiIconShown) {
                emojiButton.setImageResource(R.drawable.keyboard_icon)
                false
            } else {
                emojiButton.setImageResource(R.drawable.emoji_emotions_24px)
                true
            }
        }

        attachButton.setOnClickListener {
            val attachmentIntent = Intent(this, AttachmentsActivity::class.java)
            startActivity(attachmentIntent)
        }

        var chatChannelPhoneNumbers = ArrayList<String>()
        databaseViewModel.getChatListPhoneNumbers.observe(this, {
            chatChannelPhoneNumbers = it as ArrayList<String>
        })
        databaseViewModel.lastAddedMessage.observe(this, {
            if (it != null) {
                if (!chatChannelPhoneNumbers.contains(it.chatId)) {
                    val chatListItem =
                        ChatListItem(contactNumber = it.chatId, messageID = it.messageID)
                    databaseViewModel.createChatChannel(chatListItem)
                    Timber.d("${it.messageID} at creation====")
                } else {
                    Timber.d("${it.messageID} at update====")
                    databaseViewModel.updateChatChannel(
                        contact_number = it.chatId,
                        messageID = it.messageID.toString()
                    )
                }
            }
        })
    }

    private fun setupRecyclerView() = binding.chatRecyclerView.apply {
        chatAdapter = ChatAdapter(this@ChatActivity)
        this.adapter = chatAdapter
        this.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this@ChatActivity).apply {
            stackFromEnd = true
            isSmoothScrollbarEnabled = true
        }

    }

    override fun onResume() {
        super.onResume()
        val imagesToSend = Helper.getImages()
        if (!imagesToSend.isNullOrEmpty()) {
            viewModel.uploadImageToStorage(
                imagesToSend,
                senderIdentity,
                CalendarManager.getCurrentDateTime(),
                applicationContext
            )
        }
    }

    private fun observeUserData(phoneNumber: String) {
        lifecycleScope.launch {
            val contact = databaseViewModel.readSingleContact(phoneNumber)
            contact.observe(this@ChatActivity, {
                if (it != null) {

                    binding.chatUserName.text = it.contactName
                    Glide.with(this@ChatActivity).load(it.contactImageUrl).diskCacheStrategy(
                        DiskCacheStrategy.AUTOMATIC
                    ).placeholder(R.drawable.ic_baseline_person_color).into(binding.circleImageView)

                    userNameSendingToCallingActivity = it.contactName.toString()
                    userIDSendingToCallingActivity = it.uId.toString()
                    userImageStringSendingToCallingActivity = it.contactImageUrl.toString()

                } else {
                    binding.chatUserName.text = phoneNumber
                }
            })
        }
    }
}