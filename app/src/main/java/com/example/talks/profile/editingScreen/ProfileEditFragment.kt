package com.example.talks.profile.editingScreen

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentProfileEditBinding
import com.example.talks.networkManager.NetworkManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var databaseViewModel: UserViewModel
    private lateinit var viewModel: ProfileEditViewModel

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var inputLayout: TextInputLayout
    private lateinit var inputEditText: TextInputEditText

//    private val args: ProfileEditFragmentArgs by navArgs()

    private var isUsernameEdited: Boolean = true

    private lateinit var auth: FirebaseAuth
    private lateinit var uId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ProfileEditViewModel::class.java)
        toolbar = binding.profileEditToolbar

//        inputLayout = binding.textInputLayoutInProfileEdit
        inputEditText = binding.textInputEditText

        auth = FirebaseAuth.getInstance()
        uId = auth.currentUser!!.uid

//        if (args.EditType == 1) {
//
//            isUsernameEdited = true
//            toolbar.title = "Edit username"
//            inputLayout.hint = "Edit username"
//            inputLayout.counterMaxLength = 15
//            inputEditText.isSingleLine = true
//            inputEditText.isSelected = true
//            inputEditText.setText(args.dataString)
//
//        } else {
//
//            isUsernameEdited = false
//            toolbar.title = "Edit bio"
//            inputLayout.hint = "Edit bio"
//            inputLayout.counterMaxLength = 50
//            inputEditText.isSelected = true
//            inputEditText.setText(args.dataString)
//
//        }

        viewModel.isUserUpdated.observe(viewLifecycleOwner,{
            if (it){
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_profileEditFragment_to_profileFragment2)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.confirm_button -> {
                    if (isUsernameEdited && inputEditText.text.toString().isNotBlank()) {
                        if (NetworkManager().isDataConnected(context)) {
                            viewModel.setUsername(
                                inputEditText.text.toString(),
                                uId,
                                databaseViewModel
                            )
                        } else {
                            showErrorDialog("Operation require internet connection!")
                        }
                    }
                }
            }
            true
        }
        toolbar.inflateMenu(R.menu.edit_profile_menu)


//        binding.textInputEditText.addTextChangedListener {
//            if (isUsernameEdited) {
//                if (it.isNullOrEmpty()) {
//                    inputLayout.helperText = "username cannot be empty*"
//                } else {
//                    inputLayout.helperText = null
//                }
//            }
//        }
    }


    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle(message)
        dialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }
}