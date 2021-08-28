package com.example.talks.signup.thirdFragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.BuildConfig
import com.example.talks.Helper
import com.example.talks.R
import com.example.talks.calendar.CalendarManager
import com.example.talks.database.TalksViewModel
import com.example.talks.database.User
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.encryption.Encryption
import com.example.talks.fileManager.FileManager
import com.example.talks.gallery.GalleryActivity
import com.example.talks.home.activity.HomeScreenActivity
import com.example.talks.utils.UploadingDialog
import com.example.talks.utils.Utility
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ThirdFragment : Fragment(), TextView.OnEditorActionListener {

    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var userUid: String

    // View Binding
    private lateinit var binding: FragmentThirdBinding

    // View Models
    private val talksViewModel: TalksViewModel by viewModels()
    private val viewModel: ThirdFragmentViewModel by viewModels()

    //args
    private val args: ThirdFragmentArgs by navArgs()

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    // Variables
    private var countryName = ""
    private var countryCode = ""
    private var phoneNumber = ""
    private var retrievedImageUrl: String = ""

    // Pop up dialogs
    private lateinit var dialog: UploadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThirdBinding.inflate(inflater, container, false)
        dialog = UploadingDialog(activity as Activity)

        // data from second Fragment
        countryName = args.countryName
        countryCode = args.countryCode
        phoneNumber = args.phoneNumber

        // firebase unique user id
        userUid = auth.currentUser!!.uid

        // get user data from server if exists
        viewModel.getUserFromDatabaseIfExists(userUid)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setClickListeners()

    }

    private fun subscribeToObservers() {
        viewModel.existingUserData.observe(viewLifecycleOwner, {
            if (it != null) {
                retrievedImageUrl =
                    Encryption().decrypt(it["user_image"], encryptionKey).toString()

                binding.thirdFragmentNameEditText.setText(it["user_name"])
                binding.thirdFragmentBioEditText.setText(it["userBio"])git

                if (Helper.getImage() == null) {
                    Glide.with(this).load(retrievedImageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(binding.thirdFragmentUserImage)
                    binding.progressBar.visibility = View.GONE

                }
            } else {
                Glide.with(this).load(R.drawable.ic_baseline_person_24)
                    .into(binding.thirdFragmentUserImage)
                binding.progressBar.visibility = View.GONE
            }
        })

        viewModel.profileImageUrl.observe(viewLifecycleOwner, {
            if (it != null) {
                val image = Encryption().encrypt(it, encryptionKey)
                val user = hashMapOf(
                    "phone_number" to "$countryCode$phoneNumber",
                    "user_name" to getNameFromEditText(),
                    "user_bio" to getBioFromEditText(),
                    "user_image_url" to image,
                    "user_UID" to userUid,
                    "user_active_status" to "active now",
                )
                viewModel.addUserToFirebaseDatabase(user)
            }
        })

        viewModel.localUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                val decryptedImageUrl =
                    Encryption().decrypt(it["user_image"], encryptionKey)
                lifecycleScope.launch(Dispatchers.IO) {
                    val imageBitmap = Utility.getBitmapFromUrl(decryptedImageUrl)
                    val dbUser =
                        User(
                            phoneNumber = it["phone_number"],
                            userName = it["user_name"],
                            profileImage = imageBitmap,
                            bio = it["user_bio"],
                            firebaseAuthUID = it["user_id"]
                        )
                    talksViewModel.addUser(user = dbUser)
                }
            }
        })

//        -----------------------------Room Database VM------------------------------------
        talksViewModel.readAllUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {

                val image = Helper.getImage()
                val date = CalendarManager.getCurrentDateTime()

                FileManager().createDirectoryInExternalStorage()
                FileManager().saveProfileImageInExternalStorage(this, image, date)

                lifecycleScope.launch {
                    delay(1000L)
                    dialog.dismiss()
                    startActivity(Intent(context, HomeScreenActivity::class.java))
                    delay(500L)
                    activity?.finish()

                }
            }
        })
    }

    private fun setClickListeners() {
        binding.thirdFragmentUserImage.setOnClickListener {
            if (isPermissionGranted()) {
                navigateToGalleryActivity()
            }
        }

        binding.saveAndContinueButton.setOnClickListener {
            if (getNameFromEditText().isNotEmpty()) {
                startUploadProcess()
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
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun startUploadProcess() {
        dialog.startDialog()
        val image = Helper.getImage()

        if (image != null) {
            lifecycleScope.launch {
                viewModel.uploadImageToStorage(image, userUid)
            }
        } else {
            val encryptedImage = Encryption().encrypt(retrievedImageUrl, encryptionKey)
            val user = hashMapOf(
                "phone_number" to "$countryCode$phoneNumber",
                "user_name" to getNameFromEditText(),
                "user_bio" to getBioFromEditText(),
                "user_image_url" to encryptedImage,
                "user_UID" to userUid,
                "user_active_status" to "active now",
            )
            viewModel.addUserToFirebaseDatabase(user)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            startUploadProcess()
            binding.root.hideKeyboard()
            return true
        }
        return false
    }


    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun isPermissionGranted(): Boolean {
        return if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    Array(1) { Manifest.permission.READ_EXTERNAL_STORAGE },
                    112
                )
            }
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 112 && permissions.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            navigateToGalleryActivity()
        }
    }

    private fun navigateToGalleryActivity() {
        val intent = Intent(context, GalleryActivity::class.java)
        activity?.startActivity(intent)
    }

    private fun getNameFromEditText(): String {
        return binding.thirdFragmentNameEditText.text.toString()
    }

    private fun getBioFromEditText(): String {
        return binding.thirdFragmentBioEditText.text.toString()
    }
}