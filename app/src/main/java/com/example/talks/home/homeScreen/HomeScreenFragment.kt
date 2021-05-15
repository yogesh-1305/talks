package com.example.talks.home.homeScreen

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talks.R
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentHomeScreenBinding
import com.example.talks.home.activity.HomeScreenActivity
import com.google.firebase.auth.FirebaseAuth

class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding
    private var viewModel: HomeScreenViewModel? = null
    private lateinit var databaseViewModel: TalksViewModel

    private var chatChannelPhoneNumbers = ArrayList<String>()
    private var messagesIDs = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeScreenViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)

        val auth = FirebaseAuth.getInstance().currentUser?.uid
        val contactMap = HomeScreenActivity().contactList

        val recyclerView = binding.homeScreenRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        databaseViewModel.readChatListItem.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                for (numbers in it) {
                    val adapter = HomeScreenAdapter(it, context)
                    recyclerView.adapter = adapter
                }
            }
        })

        binding.contactsButton.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_homeScreenFragment_to_contactListActivity)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home_screen_search_button) {
            view?.let {
            }
        }
        return super.onOptionsItemSelected(item)
    }

}