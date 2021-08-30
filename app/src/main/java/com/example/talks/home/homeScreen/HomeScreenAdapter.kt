package com.example.talks.home.homeScreen

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.calendar.CalendarManager
import com.example.talks.chat.ChatActivity
import com.example.talks.database.ChatListQueriedData
import com.example.talks.databinding.ChatListItemBinding
import java.lang.IllegalArgumentException

class HomeScreenAdapter(val context: Context?) :
    RecyclerView.Adapter<HomeScreenAdapter.HomeScreenViewHolder>() {

    class HomeScreenViewHolder(val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<ChatListQueriedData>() {
        override fun areItemsTheSame(oldItem: ChatListQueriedData, newItem: ChatListQueriedData): Boolean {
            return oldItem.contact_number == newItem.contact_number
        }

        override fun areContentsTheSame(oldItem: ChatListQueriedData, newItem: ChatListQueriedData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitChatList(list:List<ChatListQueriedData>) = differ.submitList(list)

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
        if (context != null) {
            Glide.with(context).load(item.contactImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.icon_person_with_bg)
                .into(view.chatListImage)
        }
        if (item.contactName == null || item.contactName == "") {
            view.chatListName.text = item.contact_number
            Log.d("====", "${item.contact_number}====== contact number in adapter")
        } else {
            Log.d("====2", "${item.contact_number}====== contact number in adapter")
            view.chatListName.text = item.contactName
        }

        when (item.messageType){
            "/text" -> {
                view.chatListLatestMessage.text = item.messageText
            }
            "/image" -> {
                view.chatListLatestMessage.text = "Image"
            }
            "/video" -> {
                view.chatListLatestMessage.text = "Video"
            }
        }

        view.chatListTimeStamp.text = checkForDate(item.creationTime.toString())

        ///////////////////////////////////////////////////////////////////////////////////
        view.chatListItemLayout.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("contactNumber", item.contact_number)
            context?.startActivity(intent)
        }

    }

   ////////////////////////////////////////////////////////////////////////////////////////
    private val yesterdayDate = CalendarManager.getYesterdayDatDate()
    val todayDate = CalendarManager.getTodayDate()

    private fun checkForDate(timeString: String): String{
        return when (val messageDate = timeString.substring(0,10)) {
            todayDate -> {
                getTime(timeString)
            }
            yesterdayDate -> {
                "Yesterday"
            }
            else -> {
                messageDate
            }
        }
    }

    private fun getTime(timeString: String): String{
        val hours = timeString.substring(11,13).toInt()
        return when {
            hours > 12 -> {
                val newHours = hours - 12
                "$newHours:${timeString.substring(14, 16)} PM"
            }
            hours == 0 -> {
                "12:${timeString.substring(14, 16)} AM"
            }
            hours < 12 -> {
                "${timeString.substring(11,16)} AM"
            }
            else -> {
                throw IllegalArgumentException("Date Inaccurate (HomeScreenAdapter)")
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}