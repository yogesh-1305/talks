package com.example.talks.home.homeScreen

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talks.R
import com.example.talks.databinding.FragmentHomeScreenBinding
import com.google.android.material.snackbar.Snackbar

class HomeScreenFragment : Fragment() {

    private var binding: FragmentHomeScreenBinding? = null
    private var viewModel: HomeScreenViewModel? = null

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
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home_screen_search_button) {
            view?.let {
                Snackbar.make(it, "snack bar", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK"){
                        //  Dismiss Snack bar
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}