package com.example.talks.home.videoRoom

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talks.R
import com.example.talks.contactScreen.ContactViewModel
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentVideoRoomBinding

class VideoRoomFragment : Fragment() {

    private lateinit var binding: FragmentVideoRoomBinding
    private lateinit var viewModel: ContactViewModel
    private lateinit var databaseViewModel: TalksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoRoomBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contact_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}