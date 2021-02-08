package com.example.talks.signup.screens.firstFragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.talks.databinding.FragmentFirstBinding
import com.hbb20.CountryCodePicker

@Suppress("NAME_SHADOWING")
class FirstFragment : Fragment(), TextView.OnEditorActionListener{

    private lateinit var binding: FragmentFirstBinding
    private lateinit var code: CountryCodePicker
    private lateinit var number: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFirstBinding.inflate(inflater, container, false)

        code = binding.countryCodePicker
        number = binding.editTextPhone

        binding.nextButton.setOnClickListener {
            val countryCode = getCountryCode()
            val phoneNumber = getPhoneNumber()

            if (isDataConnected(context)) {
                if (phoneNumber.length == 10 && !phoneNumber.contains("/^[ A-Za-z0-9_@./#&+-]*\$/")) {
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

    private fun proceedForVerification(countryCode: String, phoneNumber: String){
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("+$countryCode$phoneNumber")
        dialog.setMessage("Confirm verify the above phone number?")
        dialog.setPositiveButton("VERIFY") { _: DialogInterface, _: Int ->
            val action =
                FirstFragmentDirections.actionFirstFragmentToSecondFragment("+$countryCode$phoneNumber")
            Navigation.findNavController(binding.root).navigate(action)
        }
        dialog.setNegativeButton("CANCEL") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isDataConnected(context: Context?): Boolean {
        val connectivityManager: ConnectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiConnected: NetworkInfo? =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val dataConnected: NetworkInfo? =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConnected != null && wifiConnected.isConnected || (dataConnected != null && dataConnected.isConnected)
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle(message)
        dialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getCountryCode(): String{
        return code.selectedCountryCode.toString()
    }

    private fun getPhoneNumber(): String{
        return number.text.toString()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE){
            val cc = getCountryCode()
            val phone = getPhoneNumber()
            proceedForVerification(cc, phone)
            return true
        }
        return false
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


}