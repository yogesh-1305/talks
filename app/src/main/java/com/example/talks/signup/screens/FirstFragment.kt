package com.example.talks.signup.screens

import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.Navigation
import com.example.talks.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class FirstFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val code = view.findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val number = view.findViewById<EditText>(R.id.editTextPhone)

        view.findViewById<Button>(R.id.next_button).setOnClickListener{
            val countryCode = code.selectedCountryCode.toString()
            val phoneNumber = number.text.toString()
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment("+$countryCode$phoneNumber")
            Navigation.findNavController(view).navigate(action)
        }

        return view
    }



}