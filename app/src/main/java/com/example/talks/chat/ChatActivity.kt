package com.example.talks.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.Helper
import com.example.talks.R
import com.example.talks.calendar.CalendarManager
import com.example.talks.calling.CallingActivity
import com.example.talks.database.ChatListItem
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityChatBinding
import com.example.talks.gallery.attachmentsGallery.activity.AttachmentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val databaseViewModel: TalksViewModel by viewModels()
    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding

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

        val viewModelFactory =
            ChatViewModelFactory(senderPhoneNumber, phoneNumber, databaseViewModel)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChatViewModel::class.java)

        if (phoneNumber != null) {
            observeUserData(phoneNumber)
            receiverID = phoneNumber
        }

        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            isSmoothScrollbarEnabled = true
        }

        val recyclerView = binding.chatRecyclerView.apply {
            setHasFixedSize(true)
            setLayoutManager(layoutManager)
        }

        lifecycleScope.launch {
            databaseViewModel.readMessages(receiverID)
                .observe(this@ChatActivity, { list ->
                    if (list.isNotEmpty()) {
                        val adapter = ChatAdapter(this@ChatActivity, list)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(applicationContext, "list is empty", Toast.LENGTH_SHORT)
                            .show()
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
                    Toast.makeText(this, "zero", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(applicationContext, imagesToSend.toString(), Toast.LENGTH_SHORT).show()
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