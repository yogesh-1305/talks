package com.example.talks.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.database.Message
import com.example.talks.databinding.CustomReceiverMessagesBinding
import com.example.talks.databinding.CustomSenderMessagesBinding

class ChatAdapter(
    val context: Context?,
    private val messages: List<Message>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SenderViewHolder(val binding: CustomSenderMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatSentText.text = message.messageText
        }
    }

    class ReceiverViewHolder(val binding: CustomReceiverMessagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.chatReceivedText.text = message.messageText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                return SenderViewHolder(
                    CustomSenderMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            2 -> {
                return ReceiverViewHolder(
                    CustomReceiverMessagesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SenderViewHolder -> holder.bind(message)
            is ReceiverViewHolder -> holder.bind(message)
            else -> throw java.lang.IllegalArgumentException("invalid view type")
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position < messages.size) {
            when (messages[position].sentByMe) {
                true -> {
                    1
                }
                false -> {
                    2
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        } else {
            0
        }
    }


    override fun getItemCount(): Int {
        return messages.size
    }
}
