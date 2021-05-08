package com.example.talks.home.homeScreen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.database.ChatListItem
import com.example.talks.databinding.ChatListItemBinding

class HomeScreenAdapter(private val chatList: List<ChatListItem>, val context: Context?) :
    RecyclerView.Adapter<HomeScreenAdapter.HomeScreenViewHolder>() {

    class HomeScreenViewHolder(val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        val item = chatList[position]

        if (context != null) {
            Glide.with(context).load(item.contactImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(view.chatListImage)
        }
        view.chatListName.text = item.contactName
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}