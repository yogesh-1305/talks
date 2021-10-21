package com.example.talks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.data.adapters.chat.activity.ChatAdapter
import com.example.talks.data.model.Message
import com.example.talks.data.model.Message.Companion.toTextMessage
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.data.viewmodels.home.activity.HomeActivityViewModel
import com.example.talks.databinding.FragmentChatBinding
import com.example.talks.others.calendar.CalendarManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding

    private val viewModel: HomeActivityViewModel by activityViewModels()
    private val dbViewModel: TalksViewModel by activityViewModels()

    private val args: ChatFragmentArgs by navArgs()
    private lateinit var chatAdapter: ChatAdapter

    private var messageText: String? = null
    private var userId: String? = null

    private var isTextEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        userId = args.chatUserPhone

        binding.btnCloseChatScreen.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        lifecycleScope.launch {
            dbViewModel.readMessages(userId.toString())
                .observe(viewLifecycleOwner, { list ->
                    list?.let {
                        chatAdapter.submitChatList(list)
                    }
                })
        }

        lifecycleScope.launch {
            dbViewModel.readSingleContact(userId.toString())
            .observe(viewLifecycleOwner, {
                if (it != null) {
                    binding.tvChatUsername.text = it.contactName
                    Glide.with(requireContext()).load(it.contactImageUrl).diskCacheStrategy(
                        DiskCacheStrategy.AUTOMATIC
                    ).placeholder(R.drawable.ic_baseline_person_color).into(binding.ivChatUserImage)

//                    userNameSendingToCallingActivity = it.contactName.toString()
//                    userIDSendingToCallingActivity = it.uId.toString()
//                    userImageStringSendingToCallingActivity = it.contactImageUrl.toString()

                }
            })
        }

        et_chat.addTextChangedListener {

            if (it != null) {
                if (it.isEmpty()) {
                    isTextEmpty = true
                    btn_chat_attach.visibility = View.VISIBLE
                    btn_mic_and_send.setImageResource(R.drawable.ic_baseline_mic_24)
                } else {
                    messageText = it.toString()
                    isTextEmpty = false
                    btn_chat_attach.visibility = View.GONE
                    btn_mic_and_send.setImageResource(R.drawable.ic_baseline_send_24)
                }
            }
        }

        btn_mic_and_send.setOnClickListener {
            when {
                isTextEmpty -> {
                    Toast.makeText(requireContext(), "mic process", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.etChat.text = null
                    val time = CalendarManager.getCurrentDateTime().toString()
                    val message = Message(
                        chatId = userId.toString(),
                        messageType = "/text",
                        messageText = this@ChatFragment.messageText,
                        status = "offline",
                        creationTime = time,
                        sentByMe = true
                    )
                    dbViewModel.addMessage(message)
                    viewModel.sendMessage(message.toTextMessage(), userId.toString())
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvChat.apply {
        chatAdapter = ChatAdapter(requireContext())
        this.adapter = chatAdapter
        this.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
            isSmoothScrollbarEnabled = true
        }

    }
}