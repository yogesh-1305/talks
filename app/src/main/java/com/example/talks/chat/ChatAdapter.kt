package com.example.talks.chat

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.database.Message
import com.example.talks.databinding.CustomReceiverMessagesBinding
import com.example.talks.databinding.CustomSenderMessagesBinding
import com.example.talks.databinding.ReceiverChatImageLayoutBinding
import com.example.talks.databinding.SenderChatImageLayoutBinding

class ChatAdapter(
    val context: Context?,
    private val messages: List<Message>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SenderTextViewHolder(val binding: CustomSenderMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatSentText.text = message.messageText
            binding.senderTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceiverTextViewHolder(val binding: CustomReceiverMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatReceivedText.text = message.messageText
            binding.receiverTextMessageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class SenderImageViewHolder(val binding: SenderChatImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatSenderImageView.setImageURI(Uri.parse(message.mediaLocalPath))
            binding.chatSenderImageTime.text = getTime(message.creationTime.toString())
        }
    }

    inner class ReceiverImageViewHolder(val binding: ReceiverChatImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatReceiverImageView.setImageURI(Uri.parse(message.mediaLocalPath))
            binding.chatReceiverImageTime.text = getTime(message.creationTime.toString())
        }
    }

    fun getTime(timeString: String): String {
        val hours = timeString.substring(11, 13).toInt()
        return when {
            hours > 12 -> {
                val newHours = hours - 12
                "$newHours:${timeString.substring(14, 16)} PM"
            }
            hours == 0 -> {
                "12:${timeString.substring(14, 16)} AM"
            }
            hours < 12 -> {
                "${timeString.substring(11, 16)} AM"
            }
            else -> {
                throw java.lang.IllegalArgumentException("Date is Inaccurate!")
            }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

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
        if (position >= messages.size) {
            return 0
        } else {
            when (messages[position].sentByMe) {
                true -> {
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
                false -> {
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
        return messages.size
    }
}
