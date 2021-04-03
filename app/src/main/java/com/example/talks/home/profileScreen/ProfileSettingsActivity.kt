package com.example.talks.home.profileScreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.talks.R
import com.example.talks.databinding.ActivityProfileSettingsBinding
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrInterface
import com.r0adkll.slidr.model.SlidrPosition


class ProfileSettingsActivity : FragmentActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var slidrInterface: SlidrInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Slidr.attach(this)
    }
}