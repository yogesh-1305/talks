package com.example.talks.gallery.attachmentsGallery.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.talks.R
import com.example.talks.databinding.ActivityAttachmentsBinding

class AttachmentsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAttachmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttachmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.attachmentActivityNavController)
        binding.AttachmentBottomNavigationView.setupWithNavController(navController)

    }


}