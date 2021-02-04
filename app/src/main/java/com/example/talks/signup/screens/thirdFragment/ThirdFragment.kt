package com.example.talks.signup.screens.thirdFragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.talks.FirebaseUser
import com.example.talks.R
import com.example.talks.database.User
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentThirdBinding
import com.example.talks.utils.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ThirdFragment : Fragment() {

    // Firebase Initialize
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // View Binding
    private lateinit var binding: FragmentThirdBinding
    // View Models
    private lateinit var userViewModel: UserViewModel
    private lateinit var viewModel: ThirdFragmentViewModel
    // Variables
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val phoneNumber = auth.currentUser?.phoneNumber
    private lateinit var dialog: LoadingDialog

    companion object {
        private var IMAGE: Uri? = null

        fun getImageUriFromMainActivity(image: Uri?) {
            IMAGE = image
        }

        fun setImageUriToFragmentImageView(): Uri? {
            return IMAGE
        }

        fun getCropImageErrorCode(code: Throwable?): Throwable? {
            return code
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

//        Log.i("phone check ===", auth.currentUser?.phoneNumber.toString())
        dialog = activity?.let { LoadingDialog(it) }!!

        viewModel.getUserFromDatabase(auth.currentUser?.phoneNumber.toString())

        binding.thirdFragmentUserImage.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_thirdFragment_to_galleryFragment)
        }

        viewModel.existingUserData.observe(viewLifecycleOwner, {
            val name = it.getUserName()
            val mail = it.getUserEmail()
            binding.thirdFragmentNameEditText.setText(name)
            binding.editTextTextEmailAddress.setText(mail)
        })

        viewModel.userAlreadyExist.observe(viewLifecycleOwner, {
            val userAlreadyExist = it
            binding.button.setOnClickListener {
                if (!userAlreadyExist) {
                    validateEmailAndUpdateUserDetails()
                    Log.i("user check====", "user not exist $it")
                } else {
                    validateEmailAndUpdateUserDetails()
                }
            }

        })

        viewModel.userCreatedInFireStore.observe(viewLifecycleOwner, {
            val userCreated = it
            val name = getNameFromEditText()
            val mail = getMailFromEditText()
            if (userCreated) {
                val localUser = phoneNumber?.let { it1 -> User(0, it1, name, mail) }
                if (localUser != null) {
                    viewModel.addUserToLocalDatabase(localUser, userViewModel)
                }
            }
        })

        observeUser()


        return binding.root
    }

    private fun observeUser() {
        viewModel.userCreatedInRoomDatabase.observe(viewLifecycleOwner, {
            val localUserCreated = it
            if (localUserCreated){
                GlobalScope.launch {
                    delay(3000L)
                    dialog.dismiss()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_thirdFragment_to_confirmation_screen)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val image = setImageUriToFragmentImageView()

        view?.let {
            Glide.with(it).load(image)
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
        val name = getNameFromEditText()
        val mail = getMailFromEditText()

        if (validateEmail(mail)) {
            dialog.startDialog()
            val user = phoneNumber?.let { it1 -> FirebaseUser(it1, name, mail, "") }
            viewModel.addUserToFirebaseFireStore(user)
        } else {
            view?.let {
                Snackbar.make(it, "Please enter a valid E-mail id", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK"){
                        //  Dismiss Snack bar
                    }.show()
            }
        }
    }

}