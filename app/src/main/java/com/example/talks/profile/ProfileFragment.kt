package com.example.talks.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentProfileBinding
import com.example.talks.encryption.Encryption
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var databaseViewModel: UserViewModel
    private lateinit var viewModel: ProfileViewModel

    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"
    var image = ""
    var userName = ""
    var phoneNumber = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var uId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        databaseViewModel.readAllUserData.observe(viewLifecycleOwner, {
            val user1 = it[0]
            userName = user1.userName
            phoneNumber = user1.phoneNumber
            image = Encryption().decrypt(user1.profileImage, encryptionKey).toString()
            Glide.with(this).load(image).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.profileScreenImage)
            binding.phoneNumberInProfile.text = phoneNumber
            binding.usernameInProfile.text = userName
            binding.profileScreenName.text = userName
        })

//        binding.changePhotoFAB.setOnClickListener {
//            Navigation.findNavController(binding.root)
//                .navigate(R.id.action_profileFragment2_to_askPhotoDestinationFragment)
//        }
//
//        binding.profileScreenBackButton.setOnClickListener {
//            activity?.finish()
//        }
//
//        binding.editPhoneNumberCard.setOnClickListener {
//            Toast.makeText(context, "Feature coming Soon", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.editUserNameButton.setOnClickListener {
//            val editName = 1
//            val action =
//                ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
//                    editName,
//                    userName
//                )
//            Navigation.findNavController(binding.root)
//                .navigate(action)
//        }
//
//        binding.editBioButton.setOnClickListener {
//            val editBio = 2
//            val action =
//                ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
//                    editBio,
//                    "bio"
//                )
//            Navigation.findNavController(binding.root)
//                .navigate(action)
//        }

        return binding.root
    }
}