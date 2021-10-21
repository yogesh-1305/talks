package com.example.talks.ui.authentication.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.talks.R
import com.example.talks.databinding.FragmentFirstBinding
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_OTP
import com.example.talks.constants.LocalConstants.KEY_AUTH_STATE
import com.example.talks.others.networkManager.NetworkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@DelicateCoroutinesApi
@Suppress("NAME_SHADOWING")
@AndroidEntryPoint
class FirstFragment : Fragment(), TextView.OnEditorActionListener {

    // view binding instance
    private lateinit var binding: FragmentFirstBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.editTextPhone.requestFocus()

        binding.nextButton.setOnClickListener {
            val countryCode = getCountryCode()
            val phoneNumber = getPhoneNumber()

            if (NetworkManager().isDataConnected(context)) {
                if (phoneNumber.length == 10) {
                    proceedForVerification(countryCode, phoneNumber)
                } else {
                    showErrorDialog("Please enter a valid phone number!")
                }
            } else {
                showErrorDialog("Please check your internet connection!")
            }
        }

        binding.editTextPhone.setOnEditorActionListener(this)

        return binding.root

    }

    private fun proceedForVerification(countryCode: String, phoneNumber: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("+$countryCode $phoneNumber")
            setMessage("Confirm verify the above phone number?")
            setPositiveButton("VERIFY") { _: DialogInterface, _: Int ->

                GlobalScope.launch {
                    prefs.edit().putString("phoneNumber", "+$countryCode$phoneNumber").apply()
                }
                navigateToSecondFragment()

            }
            setNegativeButton("CANCEL") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
        }.show()

    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle(message)
        dialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getCountryName(): String {
        return binding.countryCodePicker.selectedCountryName.toString()
    }

    private fun getCountryCode(): String {
        return binding.countryCodePicker.selectedCountryCode.toString()
    }

    private fun getPhoneNumber(): String {
        return binding.editTextPhone.text.toString()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val cc = getCountryCode()
            val phone = getPhoneNumber()
            if (NetworkManager().isDataConnected(context)) {
                proceedForVerification(cc, phone)
            } else {
                showErrorDialog("Please check your internet connection!")
            }
            return true
        }
        return false
    }

    private fun navigateToSecondFragment() {

        prefs.edit().putInt(KEY_AUTH_STATE, AUTH_STATE_ADD_OTP).apply()

        Navigation.findNavController(binding.root)
            .navigate(R.id.action_firstFragment_to_secondFragment)
    }

}