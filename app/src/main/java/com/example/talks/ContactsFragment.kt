package com.example.talks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talks.data.adapters.contact.activity.ContactAdapter
import com.example.talks.data.adapters.home.fragments.HomeScreenAdapter
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentContactsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding

    private lateinit var contactsAdapter: ContactAdapter
    private val talksViewModel: TalksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        talksViewModel.readAllContacts.observe(viewLifecycleOwner, {
            contactsAdapter.submitChatList(it)
            binding.tbContacts.subtitle = "${it.size} contacts"
        })

    }

    private fun setupRecyclerView() = binding.rvContacts.apply {
        setHasFixedSize(true)
        contactsAdapter = ContactAdapter(requireActivity())
        this.adapter = contactsAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

}