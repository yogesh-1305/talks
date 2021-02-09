package com.example.talks.home.contactScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.databinding.ContactListItemViewBinding
import com.trendyol.bubblescrollbarlib.BubbleScrollBar
import com.trendyol.bubblescrollbarlib.BubbleTextProvider

class ContactAdapter(private val contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(val binding: ContactListItemViewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(ContactListItemViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val view = holder.binding
        val contact = contacts[position]
        view.contactName.text = contact.contact_name
        view.contactNumber.text = contact.phone
    }

    override fun getItemCount(): Int {
       return contacts.size
    }

}


