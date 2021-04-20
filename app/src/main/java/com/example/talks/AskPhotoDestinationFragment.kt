package com.example.talks

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.talks.databinding.FragmentAskPhotoDestinationBinding
import com.example.talks.gallery.GalleryActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AskPhotoDestinationFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAskPhotoDestinationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAskPhotoDestinationBinding.inflate(layoutInflater, container, false)

        binding.galleryButton.setOnClickListener {
            val intent = Intent(context, GalleryActivity::class.java)
            activity?.startActivity(intent)
        }
        return binding.root
    }
}