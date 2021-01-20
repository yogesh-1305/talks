package com.example.talks.signup.screens

import android.app.AlertDialog
import android.app.SearchManager
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.talks.R
import com.hbb20.CountryCodePicker

@Suppress("NAME_SHADOWING")
class FirstFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val code = view.findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val number = view.findViewById<EditText>(R.id.editTextPhone)

        view.findViewById<Button>(R.id.next_button).setOnClickListener{
            val countryCode = code.selectedCountryCode.toString()
            val phoneNumber = number.text.toString()

            if (phoneNumber.length == 10){
                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("+$countryCode$phoneNumber")
                dialog.setMessage("Confirm verify the above phone number?")
                dialog.setPositiveButton("VERIFY"){ _: DialogInterface, _: Int ->
                    val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment("+$countryCode$phoneNumber")
                    Navigation.findNavController(view).navigate(action)
                }
                dialog.setNegativeButton("CANCEL"){ dialog: DialogInterface, _:Int ->
                    dialog.dismiss()
                }
                dialog.show()

            }else{
                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("Please Enter a Valid Phone number.")
                dialog.setPositiveButton("OK"){ dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                dialog.show()
            }
        }

        return view

    }

}