package com.example.talks.ui.contacts.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.R
import com.example.talks.data.adapters.contact.activity.ContactAdapter
import com.example.talks.data.viewmodels.contact.activity.ContactViewModel
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.ActivityContactListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView

    private val talksViewModel: TalksViewModel by viewModels()
    private val viewModel: ContactViewModel by viewModels()

    private var contactsList = ArrayList<TalksContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.contactScreenToolbar)
        supportActionBar?.title = null

        recyclerView = binding.contactsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        talksViewModel.readAllContacts.observe(this, {
            it?.let {
                contactsList = it as ArrayList<TalksContact>
                adapter = ContactAdapter(contactsList, this)
                recyclerView.adapter = adapter

                binding.contactScreenToolbar.title = "Contacts"
                binding.contactScreenToolbar.subtitle = contactsList.size.toString()
            }
        })
        binding.contactScreenToolbar.setNavigationOnClickListener { finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_screen_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.contact_screen_search_button -> {
                Toast.makeText(this, "search Button", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_screen_refresh_button -> {
                Toast.makeText(this, "refresh Button", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_screen_newContact -> {
                Toast.makeText(this, "new Contact Button", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}