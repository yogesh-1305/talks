package com.example.talks.signup

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.talks.R
import com.example.talks.databinding.FragmentLauncherBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class LauncherFragment : Fragment() {

    private lateinit var binding: FragmentLauncherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLauncherBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {
            navigateToFirstFragment()
        }

        return binding.root
    }

    private fun navigateToFirstFragment(){
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_welcomeFragment_to_firstFragment)
    }

}