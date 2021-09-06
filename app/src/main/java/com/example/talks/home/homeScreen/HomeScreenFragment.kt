package com.example.talks.home.homeScreen

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talks.R
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentHomeScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding

    private val viewModel: HomeScreenViewModel by viewModels()
    private val databaseViewModel: TalksViewModel by viewModels()

    private lateinit var homeScreenAdapter: HomeScreenAdapter

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

        setupRecyclerView()
        databaseViewModel.readChatListItem.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                   homeScreenAdapter.submitChatList(it)
            }
        })

        binding.contactsButton.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_homeScreenFragment_to_contactListActivity)
        }
        return binding.root
    }

    private fun setupRecyclerView() = binding.homeScreenRecyclerView.apply {
        homeScreenAdapter = HomeScreenAdapter(context)
        this.adapter = homeScreenAdapter
        layoutManager = LinearLayoutManager(requireContext())
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