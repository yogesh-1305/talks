package com.example.talks.profile.editingScreen

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.talks.R
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.FragmentProfileEditBinding
import com.example.talks.networkManager.NetworkManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private val databaseViewModel: TalksViewModel by viewModels()
    private val viewModel: ProfileEditViewModel by viewModels()

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var inputLayout: TextInputLayout
    private lateinit var inputEditText: TextInputEditText

    private val args: ProfileEditFragmentArgs by navArgs()

    private var isUsernameEdited: Boolean = true

    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var uId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        toolbar = binding.profileEditToolbar

        inputLayout = binding.textInputLayoutInProfileEdit
        inputEditText = binding.textInputEditText

        uId = auth.currentUser?.uid.toString()

        if (args.editType == 1) {

            isUsernameEdited = true
            toolbar.title = "Edit username"
            inputLayout.hint = "Edit username"
            inputLayout.counterMaxLength = 15
            inputEditText.isSingleLine = true
            inputEditText.isSelected = true
            inputEditText.setText(args.editString)

        } else {

            isUsernameEdited = false
            toolbar.title = "Edit bio"
            inputLayout.hint = "Edit bio"
            inputLayout.counterMaxLength = 50
            inputEditText.isSelected = true
            inputEditText.setText(args.editString)

        }

        viewModel.isUserUpdated.observe(viewLifecycleOwner, {
            if (it) {
                navigateToProfileFragment()
            }
        })

        viewModel.isBioUpdated.observe(viewLifecycleOwner, {
            if (it) {
                navigateToProfileFragment()
            }
        })

        return binding.root
    }

    private fun navigateToProfileFragment() {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_profileEditFragment_to_profileFragment2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.confirm_button -> {
                    if (NetworkManager().isDataConnected(context)) {

                        binding.editProfileProgressBar.visibility = View.VISIBLE
                        binding.textInputLayoutInProfileEdit.isEnabled = false

                        if (isUsernameEdited && inputEditText.text.toString().trim().isNotBlank()) {
                            viewModel.setUsername(
                                inputEditText.text.toString(),
                                uId,
                                databaseViewModel
                            )
                        } else if (!isUsernameEdited) {
                            Toast.makeText(context, "bio tapped", Toast.LENGTH_SHORT).show()
                            viewModel.setBio(inputEditText.text.toString(), uId, databaseViewModel)
                        }
                    } else {
                        "Please Check your Internet connection!".showErrorDialog()
                    }
                }
            }
            true
        }
        toolbar.inflateMenu(R.menu.edit_profile_menu)


        binding.textInputEditText.addTextChangedListener {
            if (isUsernameEdited) {
                if (it.isNullOrEmpty()) {
                    inputLayout.helperText = "**username cannot be empty"
                } else {
                    inputLayout.helperText = null
                }
            }
        }
    }


    private fun String.showErrorDialog() {
        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setTitle(this)
        alertDialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        alertDialog.show()
    }
}