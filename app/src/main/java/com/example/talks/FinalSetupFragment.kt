package com.example.talks

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.talks.constants.ServerConstants.FETCH_DATA_FINISHED
import com.example.talks.constants.ServerConstants.FETCH_DATA_IN_PROGRESS
import com.example.talks.constants.ServerConstants.FETCH_DATA_STARTED
import com.example.talks.data.viewmodels.authentication.activity.MainActivityViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentFinalSetupBinding
import com.example.talks.ui.home.activity.HomeScreenActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FinalSetupFragment : Fragment() {

    private lateinit var binding: FragmentFinalSetupBinding

    private val viewModel: MainActivityViewModel by activityViewModels()
    private val dbViewModel: TalksViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFinalSetupBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.imageUri != null) {
            viewModel.uploadImageToStorage()
        }

        viewModel.dataFetched.observe(viewLifecycleOwner, { dataCallback ->
            when (dataCallback) {
                FETCH_DATA_IN_PROGRESS -> {
                    binding.tvSetupInfo.text = "Restoring your messages"
                }
                FETCH_DATA_FINISHED -> {
                    binding.tvSetupInfo.text = "Restored messages successfully"
                    binding.progressBar.setProgress(100, true)
                    lifecycleScope.launch {
                        delay(1000L)
                        startActivity(Intent(context, HomeScreenActivity::class.java))
                        delay(500L)
                        activity?.finish()

                    }
                }
            }
        })

        dbViewModel.readAllUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                viewModel.readMessagesFromServer(dbViewModel)
            }
        })

    }

}