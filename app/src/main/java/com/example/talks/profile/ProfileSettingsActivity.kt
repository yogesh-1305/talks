package com.example.talks.profile

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.talks.databinding.ActivityProfileSettingsBinding
import com.r0adkll.slidr.model.SlidrInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSettingsActivity : FragmentActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var slidrInterface: SlidrInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}