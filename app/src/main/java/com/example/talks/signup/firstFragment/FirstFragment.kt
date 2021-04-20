package com.example.talks.signup.firstFragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.talks.databinding.FragmentFirstBinding
import com.example.talks.networkManager.NetworkManager

@Suppress("NAME_SHADOWING")
class FirstFragment : Fragment(), TextView.OnEditorActionListener{

    // view binding instance
    private lateinit var binding: FragmentFirstBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFirstBinding.inflate(inflater, container, false)

        binding.nextButton.setOnClickListener {
            val countryCode = getCountryCode()
            val phoneNumber = getPhoneNumber()
            val countryName = getCountryName()

            if (NetworkManager().isDataConnected(context)) {
                if (phoneNumber.length == 10) {
                    proceedForVerification(countryCode, phoneNumber, countryName)
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

    private fun proceedForVerification(countryCode: String, phoneNumber: String, countryName: String){
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("+$countryCode $phoneNumber")
        dialog.setMessage("Confirm verify the above phone number?")
        dialog.setPositiveButton("VERIFY") { _: DialogInterface, _: Int ->
            Log.i("phone number format===", getPhoneNumber())
            val action =
                FirstFragmentDirections.actionFirstFragmentToSecondFragment(phoneNumber, "+$countryCode", countryName)
            Navigation.findNavController(binding.root).navigate(action)
        }
        dialog.setNegativeButton("CANCEL") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle(message)
        dialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getCountryName(): String{
        return binding.countryCodePicker.selectedCountryName.toString()
    }

    private fun getCountryCode(): String{
        return  binding.countryCodePicker.selectedCountryCode.toString()
    }

    private fun getPhoneNumber(): String{
        return binding.editTextPhone.text.toString()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE){
            val cc = getCountryCode()
            val phone = getPhoneNumber()
            val countryName = getCountryName()
            if (NetworkManager().isDataConnected(context)) {
                proceedForVerification(cc, phone, countryName)
            }else{
                showErrorDialog("Please check your internet connection!")
            }
            return true
        }
        return false
    }

}