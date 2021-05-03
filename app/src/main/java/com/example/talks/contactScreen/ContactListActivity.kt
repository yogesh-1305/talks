package com.example.talks.contactScreen

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.R
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityContactListBinding

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ContactViewModel
    private lateinit var databaseViewModel: TalksViewModel
    private var contactsList = ArrayList<TalksContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.contactScreenToolbar)
        supportActionBar?.title = null

        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        recyclerView = binding.contactsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        databaseViewModel.readAllContacts.observe(this, {
            contactsList = it as ArrayList<TalksContact>
            adapter = ContactAdapter(contactsList, this)
            recyclerView.adapter = adapter

            binding.contactScreenToolbar.title = "Contacts"
            binding.contactScreenToolbar.subtitle = contactsList.size.toString()
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