package com.example.talks.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.Helper
import com.example.talks.calendar.CalendarManager
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentProfileBinding
import com.example.talks.encryption.Encryption
import com.example.talks.fileManager.FileManager
import com.example.talks.gallery.GalleryActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var databaseViewModel: TalksViewModel
    private lateinit var viewModel: ProfileViewModel

    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"
    var image = ""
    var userName = ""
    var userBio = ""
    var phoneNumber = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var uId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        auth = FirebaseAuth.getInstance()
        uId = auth.currentUser!!.uid.toString()
        Log.i("USER ID IN FRAGMENT PROFILE===", uId)

        databaseViewModel.readAllUserData.observe(viewLifecycleOwner, {
            val user1 = it[0]

            userName = user1.userName.toString()
            userBio = user1.userBio.toString()
            phoneNumber = user1.phoneNumber.toString()

            image = Encryption().decrypt(user1.profileImage, encryptionKey).toString()
            Glide.with(this).load(image).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.profileScreenImage)

            binding.phoneNumberInProfile.text = phoneNumber
            binding.usernameInProfile.text = userName
            binding.bioInProfile.text = userBio
            binding.profileScreenName.text = userName
        })

        binding.changePhotoFAB.setOnClickListener {
            val intent = Intent(context, GalleryActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.profileScreenBackButton.setOnClickListener {
            activity?.finish()
        }

        binding.editPhoneNumberCard.setOnClickListener {
            Toast.makeText(context, "Feature coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.editUserNameButton.setOnClickListener {
            val editName = 1
            val action =
                ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
                    editName,
                    userName
                )
            Navigation.findNavController(binding.root)
                .navigate(action)
        }

        binding.editBioButton.setOnClickListener {
            val editBio = 2
            val action =
                ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
                    editBio,
                    userBio
                )
            Navigation.findNavController(binding.root)
                .navigate(action)
        }

        viewModel.updatedProfileImageURL.observe(viewLifecycleOwner, {
            if (it != null) {
                val encryptedImageURL = Encryption().encrypt(it, encryptionKey)
                viewModel.updateImageToDatabase(encryptedImageURL, uId, databaseViewModel)
            }
        })

        viewModel.imageUpdatedInLocalDatabase.observe(viewLifecycleOwner, {
            if (it) {
                val image = Helper.getImage()
                val date = CalendarManager.getDate()
                binding.profileScreenImage.setImageURI(image)
                FileManager().createDirectoryInExternalStorage()
                FileManager().saveProfileImageInExternalStorage(this, image, date)
                Helper.setImageToNull()
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val image = Helper.getImage()
        if (image != null) {
            viewModel.updateProfileImageInStorage(image, uId)
            binding.profileFragmentProgressBar.visibility = View.VISIBLE
            Log.i("TAG IMAGE EDIT===", image.toString())
        }
    }
}