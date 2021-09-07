package com.example.talks.gallery.attachmentsGallery.videos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talks.R
import com.example.talks.databinding.FragmentVideosAttachmentBinding

class VideosAttachmentFragment : Fragment() {

    private lateinit var binding: FragmentVideosAttachmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosAttachmentBinding.inflate(inflater, container, false)


        return binding.root
    }
}