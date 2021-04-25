package com.example.talks.signup.thirdFragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.Helper
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.encryption.Encryption
import com.example.talks.gallery.GalleryActivity
import com.example.talks.home.activity.HomeScreenActivity
import com.example.talks.modal.ServerUser
import com.example.talks.utils.UploadingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThirdFragment : Fragment(), TextView.OnEditorActionListener {

    // Firebase Initialize
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String

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
    private var retrievedImageUrl: String = ""

    // Pop up dialogs
    private lateinit var dialog: UploadingDialog

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
        // nav args
        countryName = args.countryName
        countryCode = args.countryCode
        phoneNumber = args.phoneNumber

        // Firebase Initialize
        auth = FirebaseAuth.getInstance()
        userUid = auth.currentUser?.uid.toString()

        Log.i("TAG==", "UID  $userUid")
        viewModel.getUserFromDatabase(userUid)

        viewModel.existingUserData.observe(viewLifecycleOwner, {
            if (it != null) {
                retrievedImageUrl =
                    Encryption().decrypt(it.getUserProfileImage(), encryptionKey).toString()
                binding.thirdFragmentNameEditText.setText(it.getUserName())
                Log.i("TAG==", "IMAGE  $retrievedImageUrl")

                if (Helper.getImage() == null) {
                    Glide.with(this).load(retrievedImageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(binding.thirdFragmentUserImage)
                    Log.i("TAG==", "IMAGE helper is null  $retrievedImageUrl")
                    binding.progressBar.visibility = View.GONE

                }
//                else {
//                    Glide.with(this).load(Helper.getImage())
//                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                        .into(binding.thirdFragmentUserImage)
//                    Log.i("TAG==", "IMAGE helper $retrievedImageUrl")
//                    binding.progressBar.visibility = View.GONE
//
//                }
            } else {
                Glide.with(this).load(R.drawable.ic_baseline_person_color)
                    .into(binding.thirdFragmentUserImage)
                binding.progressBar.visibility = View.GONE
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
                val intent = Intent(context, GalleryActivity::class.java)
                activity?.startActivity(intent)
            }
        }

        binding.button.setOnClickListener {
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

        viewModel.profileImageUrl.observe(viewLifecycleOwner, {
            if (it != null) {
                val image = Encryption().encrypt(it, encryptionKey)
                val user = ServerUser(
                    countryName,
                    countryCode,
                    "$countryCode$phoneNumber",
                    getNameFromEditText(),
                    "$image",
                    true,
                    this.userUid
                )
                viewModel.addUserToFirebaseFireStore(user, this.userUid, userViewModel)
            }
        }
        )

        binding.thirdFragmentNameEditText.setOnEditorActionListener(this)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val image = Helper.getImage()
        Log.i("TAG==", "IMAGE helper not null  $retrievedImageUrl")
        Glide.with(binding.root).load(image)
            .into(binding.thirdFragmentUserImage)
        binding.progressBar.visibility = View.GONE
    }

    private fun getNameFromEditText(): String {
        return binding.thirdFragmentNameEditText.text.toString()
    }

    private fun startUploadProcess() {
        dialog.startDialog()
        val image = Helper.getImage()

        if (image != null) {
            lifecycleScope.launch {
                viewModel.uploadImageToStorage(image, userUid)
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            lifecycleScope.launch {
                startUploadProcess()
            }
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
//        if (requestCode == 112 && permissions.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            navigateToGallery()
//        }
    }
}