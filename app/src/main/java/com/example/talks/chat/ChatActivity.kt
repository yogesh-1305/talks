package com.example.talks.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.Helper
import com.example.talks.R
import com.example.talks.database.Message
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var databaseViewModel: TalksViewModel
    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding

    private var auth = FirebaseAuth.getInstance()
    private var senderID = auth.currentUser?.uid

    private var isTextEmpty = true
    private var messageToBeSent = ""
    private var receiverID = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fireStore: FirebaseFirestore = Firebase.firestore
        var settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        fireStore.firestoreSettings = settings
        settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        fireStore.firestoreSettings = settings

        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        val contact = Helper.getContact()
        chatUserName.isSelected = true

        if (contact != null) {
            Glide.with(this).load(contact.contactImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.circleImageView)
            binding.chatUserName.text = contact.contactName
            binding.contactStatus.text = "Active"
            receiverID = "${contact.uId}"
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
                    if (contact != null) {
                        val time = getTime()
                        val date = getDate()
                        val message = Message(
                            0,
                            messageToBeSent,
                            "sent",
                            false,
                            time,
                            date,
                            "$senderID",
                            receiverID
                        )
                        viewModel.sendMessage(senderID, receiverID, message, fireStore)
                        Toast.makeText(this, messageToBeSent, Toast.LENGTH_SHORT).show()
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