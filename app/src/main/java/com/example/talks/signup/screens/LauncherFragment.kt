package com.example.talks.signup.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.example.talks.R
import com.example.talks.databinding.FragmentLauncherBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class LauncherFragment : Fragment() {

    private lateinit var binding: FragmentLauncherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLauncherBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {
            Dexter.withContext(context)
                .withPermissions(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0 != null) {
                            if (p0.areAllPermissionsGranted()){
                                navigateToFirstFragment()
                            }else {
                                navigateToFirstFragment()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                }).check()

        }

        return binding.root
    }

    private fun navigateToFirstFragment(){
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_welcomeFragment_to_firstFragment)
    }

}