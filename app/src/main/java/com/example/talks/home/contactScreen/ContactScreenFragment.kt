package com.example.talks.home.contactScreen

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.R
import com.example.talks.database.TalksContact
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentContactScreenBinding

class ContactScreenFragment : Fragment() {

    private lateinit var binding: FragmentContactScreenBinding
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ContactViewModel
    private lateinit var databaseViewModel: UserViewModel
    private var contactsList = ArrayList<TalksContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        recyclerView = binding.contactsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        databaseViewModel.readAllContacts.observe(viewLifecycleOwner,{
            contactsList = it as ArrayList<TalksContact>
            adapter = ContactAdapter(contactsList, context)
            recyclerView.adapter = adapter
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contact_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.contact_screen_search_button) {
            Toast.makeText(context, "Search2 Tapped!", Toast.LENGTH_SHORT).show()
        } else if (item.itemId == R.id.contact_screen_search_button2) {
            Toast.makeText(context, "Search3 Tapped!", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}