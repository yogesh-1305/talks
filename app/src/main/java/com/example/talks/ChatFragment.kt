package com.example.talks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.data.adapters.chat.activity.ChatAdapter
import com.example.talks.data.model.Message
import com.example.talks.data.viewmodels.chat.activity.ChatViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.data.viewmodels.home.activity.HomeActivityViewModel
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

    //    private var userPhoneNumber: String? = null
    private var otherPersonUniqueId: String = ""

    private var isTextEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.btnCloseChatScreen.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()


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
                        binding.tvChatUsername.text = it.contactName
                        Glide.with(requireContext()).load(it.contactImageUrl).diskCacheStrategy(
                            DiskCacheStrategy.AUTOMATIC
                        ).placeholder(R.drawable.ic_baseline_person_color)
                            .into(binding.ivChatUserImage)
                        otherPersonUniqueId = it.uId.toString()
                    }
                })
        }

        binding.etChat.addTextChangedListener {

            it?.let {
                if (it.isEmpty()) {
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
                        chatId = args.chatUserPhone,
                        messageType = "/text",
                        messageText = this@ChatFragment.messageText,
                        status = "offline",
                        creationTime = time,
                        sentByMe = true
                    )
                    Log.d("add message log===", "$otherPersonUniqueId")
                    viewModel.sendMessage(message, otherPersonUniqueId, dbViewModel)
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