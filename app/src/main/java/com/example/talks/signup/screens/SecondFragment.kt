package com.example.talks.signup.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.talks.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mukesh.OtpView
import java.util.concurrent.TimeUnit

class SecondFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val args: SecondFragmentArgs by navArgs()
    private lateinit var otpView: OtpView
    private var storedVerificationId = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        otpView = view.findViewById(R.id.otpView)
        val waitingText = view.findViewById<TextView>(R.id.waiting_instructions_text_view)
        context?.let { FirebaseApp.initializeApp(it) }
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")

        val phoneNumber = args.phoneNumber
        waitingText.text = "Waiting to automatically detect an SMS sent to \'$phoneNumber\'"

        sendVerificationCode(phoneNumber)

        otpView.setOtpCompletionListener {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, it.toString())
            signInWithPhoneAuthCredentials(credential)

        }

        view.findViewById<TextView>(R.id.wrong_number_text).setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_secondFragment_to_firstFragment2)
        }

        return view
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = activity?.let {
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(it)
                .setCallbacks(callbacks)
                .build()
        }
        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            signInWithPhoneAuthCredentials(p0)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Log.i("failed verification", p0.toString())
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            storedVerificationId = verificationId
            Log.i("Verification id-----", verificationId.toString())
        }
    }

    private fun signInWithPhoneAuthCredentials(p0: PhoneAuthCredential) {
        auth.signInWithCredential(p0)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("sign in success-----", it.toString())
                    view?.let { it1 ->
                        Navigation.findNavController(it1)
                            .navigate(R.id.action_secondFragment_to_confirmation_screen)
                    }

                }
            }
    }

}