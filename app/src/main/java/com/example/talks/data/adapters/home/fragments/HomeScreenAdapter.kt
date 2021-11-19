package com.example.talks.data.adapters.home.fragments

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.constants.LocalConstants.MEDIA_MIME_TYPE_IMAGE
import com.example.talks.constants.LocalConstants.MEDIA_MIME_TYPE_TEXT
import com.example.talks.constants.LocalConstants.MEDIA_MIME_TYPE_VIDEO
import com.example.talks.data.model.HomeScreenChannelList
import com.example.talks.databinding.ChatListItemBinding
import com.example.talks.others.calendar.CalendarManager.Companion.parseTime
import com.example.talks.others.utility.ExtensionFunctions.gone
import com.example.talks.others.utility.ExtensionFunctions.show
import com.example.talks.ui.home.fragments.HomeScreenFragmentDirections

class HomeScreenAdapter(val context: Activity) :
    RecyclerView.Adapter<HomeScreenAdapter.HomeScreenViewHolder>() {

    class HomeScreenViewHolder(val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<HomeScreenChannelList>() {
        override fun areItemsTheSame(
            oldItem: HomeScreenChannelList,
            newItem: HomeScreenChannelList,
        ): Boolean {
            return oldItem.contact_number == newItem.contact_number
        }

        override fun areContentsTheSame(
            oldItem: HomeScreenChannelList,
            newItem: HomeScreenChannelList,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitChatList(list: List<HomeScreenChannelList>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeScreenViewHolder {
        return HomeScreenViewHolder(
            ChatListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeScreenViewHolder, position: Int) {
        val view = holder.binding
        val item = differ.currentList[position]

        ////////////////////////////////////////////////////////////////////////////////////

        // chat list user image
        Glide.with(context).load(item.contactImageUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.icon_person_with_bg)
            .into(view.chatListImage)

        // chat list name or number
        if (item.contactName.isNullOrEmpty()) {
            view.chatListName.text = item.contact_number
        } else {
            view.chatListName.text = item.contactName
        }

        // shows whether the message is sent or received
        if (item.sentByMe == true)
            view.sentMessageStatusImage.show()
        else
            view.sentMessageStatusImage.gone()

        // message type
        when (item.messageType) {
            MEDIA_MIME_TYPE_TEXT -> {
                view.chatListLatestMessage.text = item.messageText
            }
            MEDIA_MIME_TYPE_IMAGE -> {
                view.chatListLatestMessage.text = context.getString(R.string.text_image) // Image
            }
            MEDIA_MIME_TYPE_VIDEO -> {
                view.chatListLatestMessage.text = context.getString(R.string.text_video) // Video
            }
        }

        // latest message time
        view.chatListTimeStamp.text = item.creation_time?.parseTime()

        ///////////////////////////////////////////////////////////////////////////////////
        view.chatListItemLayout.setOnClickListener {
            val action = HomeScreenFragmentDirections
                .actionHomeScreenFragmentToChatFragment(item.contact_number.toString())

            context.findNavController(R.id.fragment_home_nav)
                .navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}