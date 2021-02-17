package com.example.talks.signup.screens.thirdFragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bumptech.glide.Glide
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.encryption.Encryption
import com.example.talks.home.activity.HomeScreenActivity
import com.example.talks.modal.ServerUser
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
    private val userUid = auth.currentUser?.uid
    // View Binding
    private lateinit var binding: FragmentThirdBinding
    // View Models
    private lateinit var userViewModel: UserViewModel
    private lateinit var viewModel: ThirdFragmentViewModel
    //args
    private val args: ThirdFragmentArgs by navArgs()
    //encryption key (v.v.imp)
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"

    // Variables
    private var countryName = ""
    private var countryCode = ""
    private var phoneNumber = ""
    private var retrievedBase64Image: String = ""
    // Pop up dialogs
    private lateinit var dialog: UploadingDialog
    private lateinit var waitingDialog: WaitingDialog

    // companion  object which retrieves image from main activity
    companion object {
        private var IMAGE: Uri? = null

        fun getImageUriFromMainActivity(image: Uri?) {
            IMAGE = image
        }

        fun getImageBitmap(): Uri? {
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
        // loading dialogs
        dialog = UploadingDialog(activity as Activity)
        waitingDialog = WaitingDialog(activity as Activity)
        waitingDialog.startDialog()
        // nav args
        countryName = args.countryName
        countryCode = args.countryCode
        phoneNumber = args.phoneNumber

        viewModel.getUserFromDatabase(userUid)

        viewModel.existingUserData.observe(viewLifecycleOwner, {
            if (it != null) {
                retrievedBase64Image =
                    Encryption().decrypt(it.getUserProfileImage(), encryptionKey).toString()
                val imageBitmap = base64ToBitmap(retrievedBase64Image)
                binding.thirdFragmentNameEditText.setText(it.getUserName())

                if (getImageBitmap() == null) {
                    Glide.with(binding.root).load(imageBitmap).placeholder(R.drawable.talks)
                        .into(binding.thirdFragmentUserImage)
                } else {
                    val image = getImageBitmap()
                    Glide.with(binding.root).load(image)
                        .placeholder(R.drawable.talks)
                        .into(binding.thirdFragmentUserImage)
                }

                waitingDialog.dismiss()

            } else {
                waitingDialog.dismiss()
                Glide.with(binding.root).load(R.drawable.talks).into(binding.thirdFragmentUserImage)
            }
        })

        viewModel.readLocalUserData(userViewModel).observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                Log.i("local user===", it.toString())
                GlobalScope.launch {
                    delay(3000L)
                    dialog.dismiss()
                    val intent = Intent(context, HomeScreenActivity::class.java)
                    startActivity(intent)
                    delay(500L)
                    activity?.finish()

                }
            }
        })

        binding.thirdFragmentUserImage.setOnClickListener {
            if (isPermissionGranted()) {
                navigateToGallery()
            }
        }

        binding.button.setOnClickListener {
            if (getNameFromEditText().isNotEmpty()) {
                lifecycleScope.launch {
                    updateUserDetails()
                }
            } else {
                Snackbar.make(binding.root, "You are required to provide a name.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK") {
                        // close Snack bar
                    }.show()
            }
        }

        binding.thirdFragmentNameEditText.setOnEditorActionListener(this)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val image = getImageBitmap()
        Glide.with(binding.root).load(image)
            .placeholder(R.drawable.talks)
            .into(binding.thirdFragmentUserImage)
    }

    private fun base64ToBitmap(image: String?): Bitmap {
        val decodedString = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun getNameFromEditText(): String {
        return binding.thirdFragmentNameEditText.text.toString()
    }

    private suspend fun updateUserDetails() {
        dialog.startDialog()
        val image = getImageBitmap()

        if (image != null) {
            val base64Image = viewModel.convertImageToBase64(getBitmap(image))
            val user = encryptAndCreateUser(base64Image)
            viewModel.addUserToFirebaseFireStore(user, userUid, userViewModel)
        } else {
            val user = encryptAndCreateUser(retrievedBase64Image)
            viewModel.addUserToFirebaseFireStore(user, userUid, userViewModel)
        }
    }

    private suspend fun getBitmap(image: Uri): Bitmap {
        val loading = ImageLoader(activity as Activity)
        val request = ImageRequest.Builder(activity as Activity)
            .data(image)
            .build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }

    private fun encryptAndCreateUser(imageUrl: String?): ServerUser {
        val encryptedString = Encryption().encrypt("$imageUrl", encryptionKey)
        val name = getNameFromEditText()
        return ServerUser(
            countryName,
            countryCode,
            "$countryCode$phoneNumber",
            name,
            "$encryptedString",
            true,
            userUid
        )
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            lifecycleScope.launch {
                updateUserDetails()
            }
            binding.root.hideKeyboard()
            return true
        }
        return false
    }

    private fun navigateToGallery() {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_thirdFragment_to_galleryFragment)
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
            navigateToGallery()
        }
    }
}