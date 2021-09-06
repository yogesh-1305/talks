package com.example.talks.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.BuildConfig
import com.example.talks.Helper
import com.example.talks.calendar.CalendarManager
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentProfileBinding
import com.example.talks.encryption.Encryption
import com.example.talks.fileManager.FileManager
import com.example.talks.gallery.GalleryActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val databaseViewModel: TalksViewModel by viewModels()
    private val viewModel: ProfileViewModel by viewModels()

    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    var image = ""
    var userName = ""
    var userBio = ""
    var phoneNumber = ""

    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var uId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        uId = auth.currentUser?.uid.toString()

        databaseViewModel.readAllUserData.observe(viewLifecycleOwner, {
            it?.let {
                val user1 = it[0]
                userName = user1.userName.toString()
                userBio = if (!user1.bio.isNullOrEmpty()) user1.bio else "Set a Bio"
                phoneNumber = user1.phoneNumber.toString()
                Glide.with(this).load(user1.profileImage)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.profileScreenImage)

                binding.apply {
                    phoneNumberInProfile.text = phoneNumber
                    usernameInProfile.text = userName
                    bioInProfile.text = userBio
                    profileScreenName.text = userName
                }
            }
        })

        binding.apply {

            // button to change photo
            changePhotoFAB.setOnClickListener {
                val intent = Intent(context, GalleryActivity::class.java)
                activity?.startActivity(intent)
            }

            // back button
            profileScreenBackButton.setOnClickListener {
                activity?.finish()
            }

            // edit phone number card
            editPhoneNumberCard.setOnClickListener {
                Toast.makeText(context, "Feature coming Soon", Toast.LENGTH_SHORT).show()
            }

            // edit user name card
            editUserNameButton.setOnClickListener {
                val editName = 1
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
                        editName,
                        userName
                    )
                Navigation.findNavController(binding.root)
                    .navigate(action)
            }

            // edit bio card
            editBioButton.setOnClickListener {
                val editBio = 2
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToProfileEditFragment(
                        editBio,
                        userBio
                    )
                Navigation.findNavController(binding.root)
                    .navigate(action)
            }
        }

        subscribeToObservers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val image = Helper.getImage()
        if (image != null) {
            viewModel.updateProfileImageInStorage(image, uId)
            binding.profileFragmentProgressBar.visibility = View.VISIBLE
        }
    }

    private fun subscribeToObservers(){

        viewModel.apply {
            updatedProfileImageURL.observe(viewLifecycleOwner, {
                if (it != null) {
                    val encryptedImageURL = Encryption().encrypt(it, encryptionKey)
                    viewModel.updateImageToDatabase(encryptedImageURL, uId, databaseViewModel)
                }
            })

            imageUpdatedInLocalDatabase.observe(viewLifecycleOwner, {
                if (it) {
                    val image = Helper.getImage()
                    val date = CalendarManager.getCurrentDateTime()
                    binding.profileScreenImage.setImageURI(image)
                    FileManager().createDirectoryInExternalStorage()
                    FileManager().saveProfileImageInExternalStorage(this@ProfileFragment, image, date)
                    Helper.setImageToNull()
                }
            })
        }

    }
}