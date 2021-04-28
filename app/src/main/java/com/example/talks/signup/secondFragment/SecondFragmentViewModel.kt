package com.example.talks.signup.secondFragment

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@Suppress("NAME_SHADOWING")
class SecondFragmentViewModel : ViewModel() {

    val smsCode: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    private var storedVerificationId = ""
    private var phoneNumber = ""
    private var countryCode = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var context: Context

    val isUserLoggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun sendVerificationCode(
        countryCode: String,
        phoneNumber: String,
        context: Context,
        auth: FirebaseAuth
    ) {
        this.auth = auth
        this.countryCode = countryCode
        this.phoneNumber = phoneNumber
        this.context = context

        viewModelScope.launch(Dispatchers.IO) {
            val options = context.let {
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("$countryCode $phoneNumber")
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(it as Activity)
                    .setCallbacks(callbacks)
                    .build()
            }
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            signInWithPhoneAuthCredentials(p0)
            smsCode.value = p0.smsCode
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(context, p0.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            storedVerificationId = verificationId
            Log.i("Verification id-----", verificationId)
        }
    }

    private fun signInWithPhoneAuthCredentials(p0: PhoneAuthCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signInWithCredential(p0)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("sign in success-----", it.toString())
                        isUserLoggedIn.value = true

                    } else {
                        isUserLoggedIn.value = false
                        Log.i("sign in failed-----", it.toString())
                    }

                }
        }
    }

    fun manualOTPAuth(otp: String?) {
        if (storedVerificationId != "") {
            val credential =
                PhoneAuthProvider.getCredential(storedVerificationId, otp.toString())
            signInWithPhoneAuthCredentials(credential)
        }
    }

}