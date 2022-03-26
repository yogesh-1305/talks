package com.example.talks.data.adapters.chat.activity

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.data.model.Message
import com.example.talks.databinding.CustomReceiverMessagesBinding
import com.example.talks.databinding.CustomSenderMessagesBinding
import com.example.talks.databinding.ReceiverChatImageLayoutBinding
import com.example.talks.databinding.SenderChatImageLayoutBinding
import java.time.LocalDateTime

class ChatAdapter(
    val context: Context?,
    private val currentUserId: String,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitChatList(list: List<Message>) = differ.submitList(list)


    inner class SentTextViewHolder(val binding: CustomSenderMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatSentText.text = message.messageText
            binding.senderTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceivedTextViewHolder(val binding: CustomReceiverMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatReceivedText.text = message.messageText
            binding.receiverTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class SentImageViewHolder(val binding: SenderChatImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatSenderImageView.setImageURI(Uri.parse(message.mediaLocalPath))
            binding.chatSenderImageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceivedImageViewHolder(val binding: ReceiverChatImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatReceiverImageView.setImageURI(Uri.parse(message.mediaLocalPath))
            binding.chatReceiverImageTime.text = getTime(message.creationTime.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(timeString: String): String {
        val time = LocalDateTime.parse(timeString)
        val hours = time.hour
        val min = time.minute
        return if (hours > 12) {
            "${hours - 12}:$min pm"
        } else {
            "$hours:$min am"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                return SentTextViewHolder(
                    CustomSenderMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            2 -> {
                return ReceivedTextViewHolder(
                    CustomReceiverMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }
            3 -> return SentImageViewHolder(
                SenderChatImageLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
            4 -> return ReceivedImageViewHolder(
                ReceiverChatImageLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = differ.currentList[position]

        when (holder) {
            is SentTextViewHolder -> holder.bind(message)
            is ReceivedTextViewHolder -> holder.bind(message)
            is SentImageViewHolder -> holder.bind(message)
            is ReceivedImageViewHolder -> holder.bind(message)
            else -> throw java.lang.IllegalArgumentException("invalid view type")
        }
    }

    private val sentTextMessageCode = 1
    private val receivedTextMessageCode = 2
    private val sentImageMessageCode = 3
    private val receivedImageMessageCode = 4

    override fun getItemViewType(position: Int): Int {
        val messages = differ.currentList
        if (position >= messages.size) {
            return 0
        } else {
            return if (messages[position].senderID == currentUserId) {
                when (messages[position].messageType) {
                    "/text" -> {
                        sentTextMessageCode
                    }
                    "/image" -> {
                        sentImageMessageCode
                    }
                    else -> throw IllegalArgumentException("Invalid view type")
                }
            } else {
                when (messages[position].messageType) {
                    "/text" -> {
                        receivedTextMessageCode
                    }
                    "/image" -> {
                        receivedImageMessageCode
                    }
                    else -> throw IllegalArgumentException("Invalid view type")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
