package com.example.talks.ui.authentication.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.BuildConfig
import com.example.talks.R
import com.example.talks.constants.LocalConstants
import com.example.talks.constants.ServerConstants.USER_BIO
import com.example.talks.constants.ServerConstants.USER_IMAGE_URL
import com.example.talks.constants.ServerConstants.USER_NAME
import com.example.talks.constants.ServerConstants.USER_PHONE_NUMBER
import com.example.talks.constants.ServerConstants.USER_STATUS
import com.example.talks.constants.ServerConstants.USER_UNIQUE_ID
import com.example.talks.data.model.TalksContact
import com.example.talks.data.model.User
import com.example.talks.data.viewmodels.authentication.activity.MainActivityViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.gallery.GalleryActivity
import com.example.talks.others.Helper
import com.example.talks.others.dialog.UploadingDialog
import com.example.talks.others.encryption.Encryption
import com.example.talks.others.fileManager.TalksStorageManager
import com.example.talks.others.utility.ConversionUtility.toBitmap
import com.example.talks.others.utility.PermissionsUtility
import com.example.talks.ui.home.activity.HomeScreenActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class ThirdFragment : Fragment(), TextView.OnEditorActionListener {

    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var userUid: String

    // View Binding
    private lateinit var binding: FragmentThirdBinding

    // View Models
    private val talksViewModel: TalksViewModel by viewModels()
    private val viewModel: MainActivityViewModel by activityViewModels()

    @Inject
    lateinit var prefs: SharedPreferences

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    // Variables
    private var phoneNumber: String? = null

    private var retrievedImageUrl: String = ""

    // Pop up dialogs
    private lateinit var dialog: UploadingDialog

    private lateinit var storagePermission: ActivityResultLauncher<String>
    private lateinit var storagePermissionBelowAndroidQ: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentThirdBinding.inflate(inflater, container, false)
        dialog = UploadingDialog(activity as Activity)

        // firebase unique user id
        userUid = auth.currentUser!!.uid

        // phone number from shared prefs
        phoneNumber = prefs.getString("phoneNumber", "")

        // get user data from server if exists
        viewModel.getUserFromDatabaseIfExists(userUid)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerForPermissionCallbacks()
        subscribeToObservers()
        setClickListeners()

        binding.thirdFragmentBioEditText.setOnEditorActionListener(this)
    }

    private fun registerForPermissionCallbacks() {
        storagePermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    navigateToGalleryActivity()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "permission denied in third fragment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        storagePermissionBelowAndroidQ =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.READ_EXTERNAL_STORAGE] == true && it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    navigateToGalleryActivity()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "permission denied in third fragment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun hasStoragePermissions(): Boolean {
        if (PermissionsUtility.hasStoragePermissions(requireContext())) return true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            storagePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        }
        return false
    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun subscribeToObservers() {

        viewModel.existingUserData.observe(viewLifecycleOwner, { userData ->
            userData?.let {

                retrievedImageUrl =
                    Encryption().decrypt(it[USER_IMAGE_URL], encryptionKey).toString()

                binding.thirdFragmentNameEditText.setText(it[USER_NAME] ?: "")
                binding.thirdFragmentBioEditText.setText(it[USER_BIO] ?: "")

                if (Helper.getImage() == null) {
                    Glide.with(this).load(retrievedImageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(binding.thirdFragmentUserImage)
                }
            }
        })

//        -----------------------------Room Database VM------------------------------------
        talksViewModel.readAllUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {

                lifecycleScope.launch {
                    prefs.edit()
                        .putInt(LocalConstants.KEY_AUTH_STATE,
                            LocalConstants.AUTH_STATE_FINAL_SETUP)
                        .apply()

                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_thirdFragment_to_finalSetupFragment)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setClickListeners() {
        binding.changeImageFab.setOnClickListener {
            if (hasStoragePermissions()) {
                navigateToGalleryActivity()
            }
        }

        binding.saveAndContinueButton.setOnClickListener {
            if (getNameFromEditText().isNotEmpty()) {
                navigateToFinalSetupFragment()
            } else {
                Snackbar.make(
                    binding.root,
                    "You are required to provide a name.",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("OK") {
                        // close Snack bar
                    }.show()
            }
        }

        binding.thirdFragmentNameEditText.setOnEditorActionListener(this)
    }

    override fun onResume() {
        super.onResume()
        val image = Helper.getImage()
        if (image != null) {
            Glide.with(binding.root).load(image).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.thirdFragmentUserImage)

            // save image in a variable in view model for final setup fragment to process it
            viewModel.imageUri = image
        } else {
            viewModel.imageUri = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return when (actionId) {
            EditorInfo.IME_ACTION_NEXT -> {
                binding.thirdFragmentBioEditText.requestFocus()
                true
            }
            EditorInfo.IME_ACTION_DONE -> {
               navigateToFinalSetupFragment()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun navigateToGalleryActivity() {
        val intent = Intent(context, GalleryActivity::class.java)
        activity?.startActivity(intent)
    }

    private fun navigateToFinalSetupFragment() {
        lifecycleScope.launch {
            prefs.edit()
                .putInt(LocalConstants.KEY_AUTH_STATE, LocalConstants.AUTH_STATE_FINAL_SETUP)
                .apply()
        }
        Navigation.findNavController(binding.root)
            .navigate(ThirdFragmentDirections.actionThirdFragmentToFinalSetupFragment(
                phoneNumber.toString(),
                getNameFromEditText(),
                getBioFromEditText(),
                userUid,
                retrievedImageUrl
            ))
    }

    private fun getNameFromEditText(): String {
        return binding.thirdFragmentNameEditText.text.toString()
    }

    private fun getBioFromEditText(): String {
        return binding.thirdFragmentBioEditText.text.toString()
    }
}