package com.example.talks.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.talks.R

class ContactScreenFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_screen, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contact_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.contact_screen_search_button){
            Toast.makeText(context, "Search2 Tapped!", Toast.LENGTH_SHORT).show()
        }else if (item.itemId == R.id.contact_screen_search_button2){
            Toast.makeText(context, "Search3 Tapped!", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}