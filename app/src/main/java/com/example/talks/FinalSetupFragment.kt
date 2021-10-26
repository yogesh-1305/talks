package com.example.talks

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.talks.constants.ServerConstants
import com.example.talks.constants.ServerConstants.FETCH_DATA_FINISHED
import com.example.talks.constants.ServerConstants.FETCH_DATA_IN_PROGRESS
import com.example.talks.constants.ServerConstants.USER_BIO
import com.example.talks.constants.ServerConstants.USER_IMAGE_URL
import com.example.talks.constants.ServerConstants.USER_NAME
import com.example.talks.constants.ServerConstants.USER_PHONE_NUMBER
import com.example.talks.constants.ServerConstants.USER_STATUS
import com.example.talks.constants.ServerConstants.USER_UNIQUE_ID
import com.example.talks.data.model.User
import com.example.talks.data.viewmodels.authentication.activity.MainActivityViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentFinalSetupBinding
import com.example.talks.others.encryption.Encryption
import com.example.talks.others.fileManager.TalksStorageManager
import com.example.talks.others.utility.ConversionUtility.toBitmap
import com.example.talks.ui.home.activity.HomeScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FinalSetupFragment : Fragment() {

    private lateinit var binding: FragmentFinalSetupBinding

    private val viewModel: MainActivityViewModel by activityViewModels()
    private val dbViewModel: TalksViewModel by activityViewModels()

    private val args: FinalSetupFragmentArgs by navArgs()

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFinalSetupBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.imageUri != null) {
            uploadImageAndUpdateDataToDatabase()
        } else {
            useExistingUserDataAndUploadToDatabase()
        }

        viewModel.localUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                val decryptedImageUrl =
                    Encryption().decrypt(it[USER_IMAGE_URL], encryptionKey)
                var imagePath = ""

                lifecycleScope.launch(Dispatchers.IO) {

                    if (viewModel.imageUri != null) {
                        val imageBitmap = viewModel.imageUri!!.toBitmap(requireActivity())
                        imagePath =
                            TalksStorageManager.saveProfilePhotoInPrivateStorage(requireContext(),
                                imageBitmap!!).toString()
                    } else {
                        val imageBitmap = async { decryptedImageUrl?.toBitmap(requireContext()) }
                        imageBitmap.await()?.let { bitmap ->
                            imagePath =
                                TalksStorageManager.saveProfilePhotoInPrivateStorage(requireContext(),
                                    bitmap).toString()
                        }
                    }

                    val dbUser = User(
                        phoneNumber = it[USER_PHONE_NUMBER],
                        userName = it[USER_NAME],
                        profileImageUrl = decryptedImageUrl,
                        bio = it[USER_BIO],
                        firebaseAuthUID = it[USER_UNIQUE_ID]
                    ).apply {
                        imageLocalPath = imagePath
                    }
                    dbViewModel.addUser(user = dbUser)
                }
            }
        })

        viewModel.dataFetched.observe(viewLifecycleOwner, { dataCallback ->
            when (dataCallback) {
                FETCH_DATA_IN_PROGRESS -> {
                    binding.tvSetupInfo.text = "Restoring your messages"
                }
                FETCH_DATA_FINISHED -> {
                    binding.tvSetupInfo.text = "Restored messages successfully"
                    binding.progressBar.setProgress(100, true)
                    navigateToHomeScreen()
                }
                else -> { /* NO-OP */
                }
            }
        })

        dbViewModel.readAllUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                viewModel.readMessagesFromServer(dbViewModel)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImageAndUpdateDataToDatabase() {
        viewModel.uploadImageToStorage()
        viewModel.profileImageUrl.observe(viewLifecycleOwner, {
            it?.let {
                val imageUrlEncrypted = Encryption().encrypt(it, encryptionKey)
                val user = hashMapOf(
                    USER_PHONE_NUMBER to args.phoneNumber,
                    USER_NAME to args.name,
                    USER_BIO to args.bio,
                    USER_IMAGE_URL to imageUrlEncrypted,
                    USER_UNIQUE_ID to args.userId,
                    USER_STATUS to "active now",
                )
                viewModel.addUserToFirebaseDatabase(user)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun useExistingUserDataAndUploadToDatabase() {
        val imageUrlEncrypted = Encryption().encrypt(args.existingImageUrl, encryptionKey)
        val user = hashMapOf(
            USER_PHONE_NUMBER to args.phoneNumber,
            USER_NAME to args.name,
            USER_BIO to args.bio,
            USER_IMAGE_URL to imageUrlEncrypted,
            USER_UNIQUE_ID to args.userId,
            USER_STATUS to "active now",
        )
        viewModel.addUserToFirebaseDatabase(user)
    }

    private fun navigateToHomeScreen() {
        lifecycleScope.launch {
            delay(1000L)
            startActivity(Intent(context, HomeScreenActivity::class.java))
            delay(500L)
            activity?.finish()
        }
    }

}