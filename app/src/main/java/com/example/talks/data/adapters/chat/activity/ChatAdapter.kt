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

    fun submitChatList(list:List<Message>) = differ.submitList(list)


    inner class SenderTextViewHolder(val binding: CustomSenderMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatSentText.text = message.messageText
            binding.senderTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceiverTextViewHolder(val binding: CustomReceiverMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatReceivedText.text = message.messageText
            binding.receiverTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class SenderImageViewHolder(val binding: SenderChatImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(message: Message) {
            binding.chatSenderImageView.setImageURI(Uri.parse(message.mediaLocalPath))
            binding.chatSenderImageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceiverImageViewHolder(val binding: ReceiverChatImageLayoutBinding) :
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
        return if (hours > 12){
            "${hours - 12}:$min pm"
        }else {
            "$hours:$min am"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                return SenderTextViewHolder(
                    CustomSenderMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            2 -> {
                return ReceiverTextViewHolder(
                    CustomReceiverMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }
            3 -> return SenderImageViewHolder(
                SenderChatImageLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
            4 -> return ReceiverImageViewHolder(
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
            is SenderTextViewHolder -> holder.bind(message)
            is ReceiverTextViewHolder -> holder.bind(message)
            is SenderImageViewHolder -> holder.bind(message)
            is ReceiverImageViewHolder -> holder.bind(message)
            else -> throw java.lang.IllegalArgumentException("invalid view type")
        }
    }

    private val senderTextMessageCode = 1
    private val receiverTextMessageCode = 2
    private val senderImageMessageCode = 3
    private val receiverImageMessageCode = 4

    override fun getItemViewType(position: Int): Int {
        val messages = differ.currentList
        if (position >= messages.size) {
            return 0
        } else {
            when (messages[position].senderID) {
                "true" -> {
                    return when (messages[position].messageType) {
                        "/text" -> {
                            senderTextMessageCode
                        }
                        "/image" -> {
                            senderImageMessageCode
                        }
                        else -> throw IllegalArgumentException("Invalid view type")
                    }
                }
                "false" -> {
                    return when (messages[position].messageType) {
                        "/text" -> {
                            receiverTextMessageCode
                        }
                        "/image" -> {
                            receiverImageMessageCode
                        }
                        else -> throw IllegalArgumentException("Invalid view type")
                    }
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
