package com.example.talks.home.contactScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.R
import com.example.talks.databinding.FragmentContactScreenBinding
import com.trendyol.bubblescrollbarlib.BubbleTextProvider

class ContactScreenFragment : Fragment() {

    private lateinit var binding: FragmentContactScreenBinding
    private lateinit var contactList : MutableList<Contact>
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        contactList = ArrayList()
        viewModel.checkUsers()

        viewModel.users.observe(viewLifecycleOwner,{
            val users = it
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.contactsRecyclerView
        val scrollBar = binding.bubbleScrollBar
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        scrollBar.attachToRecyclerView(recyclerView)
        adapter = ContactAdapter(contactList)
        recyclerView.adapter = adapter
        scrollBar.bubbleTextProvider = BubbleTextProvider {adapter.itemCount.toString()}
        if (isPermissionGranted()) {
            readContacts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PERMISSION_GRANTED) {
            readContacts()
        }
    }

    @SuppressLint("Recycle")
    private fun readContacts() {
        val phones = context?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        if (phones != null) {
           showContacts(phones)
        }
    }

    private fun showContacts(phones : Cursor){
        while (phones.moveToNext()) {
            val contactName =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phoneNumber = phoneNumber.replace("\\s".toRegex(),"")
            val contact = Contact(phoneNumber, contactName)
            contactList.add(contact)
            adapter.notifyDataSetChanged()
        }

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

    private fun isPermissionGranted(): Boolean {
        return if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CONTACTS
                )
            } != PERMISSION_GRANTED) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    Array(1) { Manifest.permission.READ_CONTACTS },
                    111
                )
            }
            false
        } else {
            true
        }
    }
}