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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.example.talks.data.viewmodels.authentication.fragments.ThirdFragmentViewModel
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
    private val viewModel: ThirdFragmentViewModel by viewModels()

    @Inject
    lateinit var prefs: SharedPreferences

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    // Variables
    private var phoneNumber: String? = null

    private var retrievedImageUrl: String = ""

    // Pop up dialogs
    private lateinit var dialog: UploadingDialog

    private var contactPhoneNumberList = ArrayList<String>()
    private var contactNameList = HashMap<String, String>()
    private var dataFetchedOnce = false

    private lateinit var storagePermission: ActivityResultLauncher<String>
    private lateinit var storagePermissionBelowAndroidQ: ActivityResultLauncher<Array<String>>
    private lateinit var contactPermission: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        subscribeToObservers()
        setClickListeners()

        registerForPermissionCallbacks()
        requestContactsPermission()

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

        contactPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    readContacts()
                } else {
                    TODO("handle permission denial")
                }
            }
    }

    private fun requestContactsPermission() {
        if (PermissionsUtility.hasContactsPermissions(requireContext())) return
        contactPermission.launch(Manifest.permission.READ_CONTACTS)
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
        viewModel.existingUserData.observe(viewLifecycleOwner, {
            if (it != null) {
                retrievedImageUrl =
                    Encryption().decrypt(it[USER_IMAGE_URL], encryptionKey).toString()

                binding.thirdFragmentNameEditText.setText(it[USER_NAME])
                binding.thirdFragmentBioEditText.setText(it[USER_BIO])

                if (Helper.getImage() == null) {
                    Glide.with(this).load(retrievedImageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(binding.thirdFragmentUserImage)
                }
            } else {
                Glide.with(this).load(R.drawable.ic_baseline_person_24)
                    .into(binding.thirdFragmentUserImage)
            }
        })

        viewModel.profileImageUrl.observe(viewLifecycleOwner, {
            if (it != null) {
                val image = Encryption().encrypt(it, encryptionKey)
                val user = hashMapOf(
                    USER_PHONE_NUMBER to "$phoneNumber",
                    USER_NAME to getNameFromEditText(),
                    USER_BIO to getBioFromEditText(),
                    USER_IMAGE_URL to image,
                    USER_UNIQUE_ID to userUid,
                    USER_STATUS to "active now",
                )
                viewModel.addUserToFirebaseDatabase(user)
            }
        })

        viewModel.localUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                val decryptedImageUrl =
                    Encryption().decrypt(it[USER_IMAGE_URL], encryptionKey)

                lifecycleScope.launch(Dispatchers.IO) {

                    val imageBitmap = async { decryptedImageUrl?.toBitmap(requireContext()) }
                    val imagePath = imageBitmap.await()?.let { bitmap ->
                        TalksStorageManager.saveProfilePhotoInPrivateStorage(requireContext(),
                            bitmap
                        )
                    }
                    val dbUser =
                        User(
                            phoneNumber = it[USER_PHONE_NUMBER],
                            userName = it[USER_NAME],
                            profileImageUrl = decryptedImageUrl,
                            bio = it[USER_BIO],
                            firebaseAuthUID = it[USER_UNIQUE_ID]
                        ).apply {
                            imageLocalPath = imagePath
                        }
                    talksViewModel.addUser(user = dbUser)
                }
            }
        })

//        -----------------------------Room Database VM------------------------------------
        talksViewModel.readAllUserData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {

//                val image = Helper.getImage()
//                val date = CalendarManager.getCurrentDateTime()

                lifecycleScope.launch {
                    prefs.edit().putInt(LocalConstants.KEY_AUTH_STATE, LocalConstants.AUTH_STATE_COMPLETE).apply()
                    delay(1000L)
                    dialog.dismiss()
                    startActivity(Intent(context, HomeScreenActivity::class.java))
                    delay(500L)
                    activity?.finish()

                }
            }
        })

        lifecycleScope.launch {
            talksViewModel.readContactPhoneNumbers.observe(viewLifecycleOwner, {
                if (it != null) {
                    if (!dataFetchedOnce) {
                        viewModel.getUsersFromServer(
                            it,
                            contactNameList,
                            talksViewModel,
                            encryptionKey
                        )
                        dataFetchedOnce = true
                    }
                }
            })
        }
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
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startUploadProcess() {
        dialog.startDialog()
        val image = Helper.getImage()
        val imageBitmap = image?.toBitmap(requireActivity())

        if (imageBitmap != null) {
            lifecycleScope.launch {
                viewModel.uploadImageToStorage(image, userUid)
                imageBitmap.let {
                    TalksStorageManager.saveProfilePhotoInPrivateStorage(
                        requireContext(),
                        it
                    )
                }
            }
        } else {
            lifecycleScope.launch {
                val encryptedImage = Encryption().encrypt(retrievedImageUrl, encryptionKey)
                val user = hashMapOf(
                    USER_PHONE_NUMBER to "$phoneNumber",
                    USER_NAME to getNameFromEditText(),
                    USER_BIO to getBioFromEditText(),
                    USER_IMAGE_URL to encryptedImage,
                    USER_UNIQUE_ID to userUid,
                    USER_STATUS to "active now",
                )
                viewModel.addUserToFirebaseDatabase(user)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun readContacts() {
        val phones = requireActivity().contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        if (phones != null) {
            showContacts(phones)
        }
    }

    @SuppressLint("Range")
    private fun showContacts(phones: Cursor) {
        lifecycleScope.launch {
            while (phones.moveToNext()) {
                val contactName =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var phoneNumber =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                phoneNumber = phoneNumber.replace("\\s".toRegex(), "").trim()
                phoneNumber = formatPhoneNumber(phoneNumber)
                contactPhoneNumberList.add(phoneNumber)
                contactNameList[phoneNumber] = contactName
                val contact = TalksContact(
                    phoneNumber, contactName
                )
                talksViewModel.addContact(contact)
            }
        }
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        var number = phoneNumber
        if (!number.startsWith("+")) {
            number = if (phoneNumber.startsWith("0")) {
                number = phoneNumber.drop(1)
                "+91$number"
            } else {
                "+91$number"
            }
        }
        return number
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