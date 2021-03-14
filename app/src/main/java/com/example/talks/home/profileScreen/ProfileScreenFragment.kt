package com.example.talks.home.profileScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentProfileScreenBinding
import com.example.talks.encryption.Encryption

class ProfileScreenFragment : Fragment() {
    private lateinit var binding: FragmentProfileScreenBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var databaseViewModel: UserViewModel
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileScreenBinding.inflate(inflater, container, false)

        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        databaseViewModel.readAllUserData.observe(viewLifecycleOwner, {
            for (data in it) {
                binding.profileName.text = data.userName
                binding.profileStatus.text = data.phoneNumber
                val image = Encryption().decrypt(data.profileImage, encryptionKey)
                Glide.with(this).load(image).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.profileImage)

            }
        })

        return binding.root
    }

}