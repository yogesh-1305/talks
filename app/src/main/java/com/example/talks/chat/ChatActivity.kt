package com.example.talks.chat

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.ContactHelper
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.ActivityChatBinding
import com.example.talks.modal.MessageSchema
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var databaseViewModel: UserViewModel
    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding

    private var auth = FirebaseAuth.getInstance()
    private var uid = auth.currentUser?.uid

    private var isTextEmpty = true
    private var messageToBeSent = ""
    private var userUid = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        val contact = ContactHelper.getContact()
        chatUserName.isSelected = true

        if (contact != null) {
            Glide.with(this).load(contact.imageUrl).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.circleImageView)
            binding.chatUserName.text = contact.userName
            binding.contactStatus.text = "Active"
            userUid = contact.uId
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
                    Toast.makeText(this, messageToBeSent, Toast.LENGTH_SHORT).show()
                    if (contact != null) {
                        val time = getTime()
                        val date = getDate()
                        val message = MessageSchema(messageToBeSent, time, date, userUid, false)
                        viewModel.sendMessage(uid, userUid, message)
                    }

                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String {
        val today = Date()
        val format = SimpleDateFormat("dd-mm-yyyy")
        return format.format(today)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String {
        val today = Date()
        val format = SimpleDateFormat("hh:mm")
        return format.format(today)
    }
}