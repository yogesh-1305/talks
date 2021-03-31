package com.example.talks.home.contactScreen

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.ContactHelper
import com.example.talks.chat.ChatActivity
import com.example.talks.database.TalksContact
import com.example.talks.databinding.ContactListItemViewBinding

class ContactAdapter(private val contacts: List<TalksContact>, val context: Context?) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(val binding: ContactListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            ContactListItemViewBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val view = holder.binding
        val contact = contacts[position]

        view.contactName.text = contact.userName
        view.contactNumber.text = contact.number

        val contactImage = contact.imageUrl
        if (context != null) {
            Glide.with(context).load(contactImage).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(view.contactImage)
        }

        view.listItemLayout.setOnClickListener {
            val name = contact.userName
            val number = contact.number
            val uid = contact.uId
            val contactHelper = TalksContact(number, name, contactImage, uid)
            ContactHelper.setContact(contactHelper)
            val intent = Intent(context, ChatActivity::class.java)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

}


