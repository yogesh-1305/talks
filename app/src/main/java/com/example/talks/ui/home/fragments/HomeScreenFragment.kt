package com.example.talks.ui.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talks.R
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentHomeScreenBinding
import com.example.talks.data.adapters.home.fragments.HomeScreenAdapter
import com.example.talks.data.viewmodels.home.fragments.HomeScreenViewModel
import com.example.talks.ui.contacts.activity.ContactListActivity
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding

    private val viewModel: HomeScreenViewModel by activityViewModels()
    private val databaseViewModel: TalksViewModel by viewModels()

    private lateinit var homeScreenAdapter: HomeScreenAdapter

    private lateinit var fragmentToolbar: MaterialToolbar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        fragmentToolbar = binding.tbHome
//        inflateMenu()
        setupRecyclerView()
        subscribeToObservers()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateMenu()
    }

    private fun inflateMenu() {

        fragmentToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.home_screen_search_button -> {
                    Toast.makeText(requireContext(), "Search Button Tap", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root).navigate(R.id.action_homeScreenFragment_to_chatFragment)
                }
                R.id.home_screen_starred_button -> {
                    startActivity(Intent(requireContext(), ContactListActivity::class.java))
                }
                R.id.home_screen_web_button -> {
                }
                R.id.home_screen_setting_button -> {
                }
            }
            true
        }
    }

    private fun subscribeToObservers() {
        databaseViewModel.readHomeScreenChannelList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                homeScreenAdapter.submitChatList(it)
            }
        })
    }

    private fun setupRecyclerView() = binding.homeScreenRecyclerView.apply {
        homeScreenAdapter = HomeScreenAdapter(requireActivity())
        this.adapter = homeScreenAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}