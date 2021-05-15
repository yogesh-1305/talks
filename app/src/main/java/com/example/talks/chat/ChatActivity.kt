package com.example.talks.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.database.Message
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var databaseViewModel: TalksViewModel
    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding

    private var auth = FirebaseAuth.getInstance()
    private var senderID = auth.currentUser?.phoneNumber

    private var isTextEmpty = true
    private var messageToBeSent = ""
    private var receiverID = ""

    private lateinit var emojiPopup: EmojiPopup

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        val intent = intent
        val phoneNumber = intent.getStringExtra("contactNumber")

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
                    Log.i("messages list check+++", list.toString())
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

        micButton.setOnClickListener {
            when {
                isTextEmpty -> {
                    Toast.makeText(this, "mic process", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.messageEditText.text = null
                    val time = getTime()
                    val date = getDate()
                    val message = Message(
                        receiverID,
                        "",
                        messageToBeSent,
                        "sent",
                        false,
                        time,
                        date,
                        true
                    )
                    viewModel.sendMessage(
                        senderID,
                        receiverID,
                        messageToBeSent,
                        time,
                        date,
                        databaseViewModel
                    )
                    Toast.makeText(this, messageToBeSent, Toast.LENGTH_SHORT).show()

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

    }

    private fun observeUserData(phoneNumber: String) {
        lifecycleScope.launch {
            val contact = databaseViewModel.readSingleContact(phoneNumber)
            contact.observe(this@ChatActivity, {
                binding.chatUserName.text = it.contactName

                Glide.with(this@ChatActivity).load(it.contactImageUrl).diskCacheStrategy(
                    DiskCacheStrategy.AUTOMATIC
                ).placeholder(R.drawable.ic_baseline_person_color).into(binding.circleImageView)
            })
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String {
        val today = Date()
        val format = SimpleDateFormat("dd-MM-yyyy")
        return format.format(today)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String {
        val today = Date()
        val format = SimpleDateFormat("hh:mm")
        return format.format(today)
    }
}