package com.example.talks.signup.screens.thirdFragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.talks.R
import com.example.talks.database.User
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.modal.FirebaseUser
import com.example.talks.utils.UploadingDialog
import com.example.talks.utils.WaitingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThirdFragment : Fragment(), TextView.OnEditorActionListener {

    // Firebase Initialize
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // View Binding
    private lateinit var binding: FragmentThirdBinding
    // View Models
    private lateinit var userViewModel: UserViewModel
    private lateinit var viewModel: ThirdFragmentViewModel
    //args
    private val args: ThirdFragmentArgs by navArgs()
    // Variables
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    private var countryName = ""
    private var countryCode = ""
    private var phoneNumber = ""

    private lateinit var dialog: UploadingDialog
    private var retrievedImageUrl: String = ""
    private lateinit var waitingDialog: WaitingDialog

    companion object {
        private var IMAGE: Uri? = null

        fun getImageUriFromMainActivity(image: Uri?) {
            IMAGE = image
        }

        fun getImageUri(): Uri? {
            return IMAGE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThirdBinding.inflate(inflater, container, false)
        // User viewModel to access the Room Database
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ThirdFragmentViewModel::class.java)

        dialog = UploadingDialog(activity as Activity)
        waitingDialog = WaitingDialog(activity as Activity)
        waitingDialog.startDialog()

        countryName = args.countryName
        countryCode = args.countryCode
        phoneNumber = args.phoneNumber

        viewModel.getUserFromDatabase(phoneNumber, countryName, countryCode)

        binding.thirdFragmentUserImage.setOnClickListener {
            if (isPermissionGranted()) {
                navigateToGallery()
            }
        }

        viewModel.existingUserData.observe(viewLifecycleOwner, {
            if(it != null) {
                val name = it.getUserName()
                val mail = it.getUserEmail()
                retrievedImageUrl = it.getUserProfileImage()
                binding.thirdFragmentNameEditText.setText(name)
                binding.editTextTextEmailAddress.setText(mail)
                Glide.with(binding.root).load(retrievedImageUrl).placeholder(R.drawable.talks)
                    .into(binding.thirdFragmentUserImage)
                waitingDialog.dismiss()
            }else{
                waitingDialog.dismiss()
            }
        })


        binding.button.setOnClickListener {
            validateEmailAndUpdateUserDetails()
        }

        binding.editTextTextEmailAddress.setOnEditorActionListener(this)

        observeUserCreationInFireStoreDatabase()

        observeUserCreationInRoomDatabase()

        observeImageUrl()

        return binding.root
    }

    private fun observeUserCreationInFireStoreDatabase() {
        viewModel.userCreatedInFireStore.observe(viewLifecycleOwner, {
            val userCreated = it
            val name = getNameFromEditText()
            val mail = getMailFromEditText()
            if (userCreated) {
                val localUser = User(0, phoneNumber, name, mail)
                viewModel.addUserToLocalDatabase(localUser, userViewModel)
            }
        })
    }

    private fun observeUserCreationInRoomDatabase() {
        viewModel.userCreatedInRoomDatabase.observe(viewLifecycleOwner, {
            val localUserCreated = it
            if (localUserCreated) {
                GlobalScope.launch {
                    delay(3000L)
                    dialog.dismiss()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_thirdFragment_to_confirmation_screen)
                }
            }
        })
    }

    private fun observeImageUrl() {
        viewModel.profileImageUrl.observe(viewLifecycleOwner, {
            val imageUrl = it
            val name = getNameFromEditText()
            val mail = getMailFromEditText()
            if (imageUrl != null) {
                if (!validateEmail(mail)) {
                    createSnackBar("Please Enter a valid E-mail address")
                } else {
                    val user = FirebaseUser(countryName, countryCode, phoneNumber, name, mail, imageUrl, true)
                    viewModel.addUserToFirebaseFireStore(user)
                }
            } else {
                val user =
                    FirebaseUser( countryName, countryCode, phoneNumber, name, mail, retrievedImageUrl, true)
                viewModel.addUserToFirebaseFireStore(user)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val image = getImageUri()
//        Log.i("image check=== 1", image.toString())
//
//        Log.i("image check=== not null", image.toString())
        if (image != null) {
            Glide.with(binding.root).load(image)
                .placeholder(R.drawable.talks)
                .into(binding.thirdFragmentUserImage)
        }
    }

    private fun getNameFromEditText(): String {
        return binding.thirdFragmentNameEditText.text.toString()
    }

    private fun getMailFromEditText(): String {
        return binding.editTextTextEmailAddress.text.toString()
    }

    private fun validateEmail(mail: String): Boolean {
        return mail.matches(emailPattern.toRegex()) || mail.trim().isEmpty()
    }

    private fun validateEmailAndUpdateUserDetails() {
        if (validateEmail(getMailFromEditText())) {
            dialog.startDialog()
            val image = getImageUri()
            viewModel.uploadImageToStorage(image, "+$countryCode $phoneNumber")
        }else{
            createSnackBar("Please enter a valid e-mail address")
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            validateEmailAndUpdateUserDetails()
            binding.root.hideKeyboard()
            return true
        }
        return false
    }

    private fun navigateToGallery(){
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_thirdFragment_to_galleryFragment)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun createSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("OK") {
                // close Snack bar
            }.show()
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
        if (requestCode == 112 && permissions.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
            navigateToGallery()
        }
    }
}