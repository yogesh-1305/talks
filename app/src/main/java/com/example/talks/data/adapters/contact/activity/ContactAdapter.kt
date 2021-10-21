package com.example.talks.data.adapters.contact.activity

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.ContactsFragmentDirections
import com.example.talks.R
import com.example.talks.data.model.TalksContact
import com.example.talks.databinding.ContactListItemViewBinding

class ContactAdapter(val context: Activity) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<TalksContact>() {
        override fun areItemsTheSame(
            oldItem: TalksContact,
            newItem: TalksContact
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: TalksContact,
            newItem: TalksContact
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitChatList(list: List<TalksContact>) = differ.submitList(list)

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
        val contact = differ.currentList[position]

        view.contactName.text = contact.contactName
        view.contactNumber.text = contact.contactNumber

        val contactImage = contact.contactImageUrl
        if (context != null) {
            Glide.with(context).load(contactImage).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(
                    R.drawable.ic_baseline_person_color
                )
                .into(view.contactImage)
        }

        view.listItemLayout.setOnClickListener {

            // send contact phone number and navigate to chat screen
            context.findNavController(R.id.fragment_home_nav)
                .navigate(
                    ContactsFragmentDirections.actionContactsFragmentToChatFragment(
                        chatUserPhone = contact.contactNumber.toString()
                    )
                )
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}


