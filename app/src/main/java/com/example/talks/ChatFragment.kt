package com.example.talks

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.constants.LocalConstants.MEDIA_MIME_TYPE_TEXT
import com.example.talks.constants.LocalConstants.MESSAGE_PENDING
import com.example.talks.data.adapters.chat.activity.ChatAdapter
import com.example.talks.data.model.Message
import com.example.talks.data.viewmodels.chat.activity.ChatViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentChatBinding
import com.example.talks.others.calendar.CalendarManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding

    private val viewModel: ChatViewModel by viewModels()
    private val dbViewModel: TalksViewModel by viewModels()

    // receives the contact phone number from either home or contact screen
    private val args: ChatFragmentArgs by navArgs()

    private lateinit var chatAdapter: ChatAdapter

    private var messageText: String? = null

    private var otherPersonUniqueId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        subscribeToObservers()

        setClickListeners()
    }

    private fun subscribeToObservers() {
        lifecycleScope.launch {
            dbViewModel.readMessages(args.chatUserPhone)
                .observe(viewLifecycleOwner, { list ->
                    list?.let {
                        chatAdapter.submitChatList(list)
                    }
                })
        }

        lifecycleScope.launch {
            dbViewModel.readSingleContact(args.chatUserPhone)
                .observe(viewLifecycleOwner, {
                    if (it != null) {

                        // setting name on screen
                        binding.tvChatUsername.text =
                            if (it.contactName.isNullOrEmpty()) it.contactNumber else it.contactName

                        // setting user image
                        Glide.with(requireContext()).load(it.contactImageUrl).diskCacheStrategy(
                            DiskCacheStrategy.AUTOMATIC
                        ).placeholder(R.drawable.ic_baseline_person_color)
                            .into(binding.ivChatUserImage)

                        otherPersonUniqueId = it.uId.toString()
                    }
                })
        }
    }

    private fun setClickListeners() {
        var isTextEmpty = true
        binding.etChat.addTextChangedListener {

            it?.let {
                if (it.isBlank()) {
                    isTextEmpty = true
                    binding.btnChatAttach.visibility = View.VISIBLE
                    binding.btnMicAndSend.setImageResource(R.drawable.ic_baseline_mic_24)
                } else {
                    messageText = it.toString()
                    isTextEmpty = false
                    binding.btnChatAttach.visibility = View.GONE
                    binding.btnMicAndSend.setImageResource(R.drawable.ic_baseline_send_24)
                }
            }
        }

        binding.btnMicAndSend.setOnClickListener {
            when {
                isTextEmpty -> {
                    Toast.makeText(requireContext(), "mic process", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.etChat.text = null
                    val time = CalendarManager.getCurrentDateTime().toString()
                    val message = Message(
                        chatID = args.chatUserPhone,
                        messageType = MEDIA_MIME_TYPE_TEXT,
                        messageText = this.messageText?.trim(),
                        creationTime = time,
                    )
                    viewModel.sendMessage(message, otherPersonUniqueId, dbViewModel)
                }
            }
        }

        binding.btnCloseChatScreen.setOnClickListener {
            requireActivity().onBackPressed()
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