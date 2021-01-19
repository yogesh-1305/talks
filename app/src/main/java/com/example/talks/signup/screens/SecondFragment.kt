package com.example.talks.signup.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private var storedVerificationId : String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        otpView = view.findViewById(R.id.otpView)

        context?.let { FirebaseApp.initializeApp(it) }
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")

        val phoneNumber = args.phoneNumber
        sendVerificationCode(phoneNumber)

        otpView.setOtpCompletionListener {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, it.toString())
            signInWithPhoneAuthCredentials(credential)
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
//            Log.i("phone verification", p0.smsCode.toString())
//            verificationCode = p0.smsCode.toString().trim()
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
            Log.i("oncodesnt-------", verificationId)
            storedVerificationId = verificationId

        }

    }

    private fun signInWithPhoneAuthCredentials(p0: PhoneAuthCredential) {
        auth.signInWithCredential(p0)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("sign in success-----", it.toString())
                }
            }
    }
}