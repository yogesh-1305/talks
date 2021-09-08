package com.example.talks.signup.thirdFragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import androidx.navigation.fragment.navArgs
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.BuildConfig
import com.example.talks.R
import com.example.talks.calendar.CalendarManager
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.database.User
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.encryption.Encryption
import com.example.talks.fileManager.TalksStorageManager
import com.example.talks.gallery.GalleryActivity
import com.example.talks.home.activity.HomeScreenActivity
import com.example.talks.others.Helper
import com.example.talks.others.utility.ConversionUtility.toBitmap
import com.example.talks.others.utility.PermissionsUtility
import com.example.talks.utils.UploadingDialog
import com.example.talks.utils.Utility.toBitmap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.net.URL
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

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    //args
    private val args: ThirdFragmentArgs by navArgs()

    // Variables
    private var countryName = ""
    private var countryCode = ""
    private var phoneNumber = ""

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

        countryName = args.countryName
        countryCode = args.countryCode
        phoneNumber = args.phoneNumber

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
                    Encryption().decrypt(it["user_image"], encryptionKey).toString()

                binding.thirdFragmentNameEditText.setText(it["user_name"])
                binding.thirdFragmentBioEditText.setText(it["userBio"])

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
                    val imageBitmap = async { decryptedImageUrl?.toBitmap(requireContext()) }
                    val imagePath = imageBitmap.await()?.let { bitmap ->
                        TalksStorageManager.saveProfilePhotoInPrivateStorage(requireContext(),
                            bitmap
                        )
                    }
                    val dbUser =
                        User(
                            phoneNumber = it["phone_number"],
                            userName = it["user_name"],
                            profileImageUrl = decryptedImageUrl,
                            bio = it["user_bio"],
                            firebaseAuthUID = it["user_id"]
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

                val image = Helper.getImage()
                val date = CalendarManager.getCurrentDateTime()

                lifecycleScope.launch {
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
        binding.thirdFragmentUserImage.setOnClickListener {
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
            binding.progressBar.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startUploadProcess() {
        dialog.startDialog()
        val image = Helper.getImage()
        val imageBitmap = image?.toBitmap(requireActivity())

        if (image != null) {
            lifecycleScope.launch {
                viewModel.uploadImageToStorage(image, userUid)
                imageBitmap?.let {
                    TalksStorageManager.saveProfilePhotoInPrivateStorage(
                        requireContext(),
                        it
                    )
                }
            }
        } else {
            lifecycleScope.launch() {
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