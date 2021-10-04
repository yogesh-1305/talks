package com.example.talks.data.viewmodels.authentication.fragments

import android.app.Activity
import android.content.Context
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
    val verificationID: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    private var storedVerificationId = ""
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    val isUserLoggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun sendVerificationCode(
        phoneNumber: String?,
        activity: Activity,
        auth: FirebaseAuth
    ) {
        if (phoneNumber != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val options = activity.let {
                    PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(it)
                        .setCallbacks(callbacks)
                        .build()
                }
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            signInWithPhoneAuthCredentials(p0)
            smsCode.value = p0.smsCode
        }

        override fun onVerificationFailed(p0: FirebaseException) {
//            Toast.makeText(context, p0.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            storedVerificationId = verificationId
            verificationID.value = verificationId
        }
    }

    private fun signInWithPhoneAuthCredentials(p0: PhoneAuthCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signInWithCredential(p0)
                .addOnCompleteListener {
                    isUserLoggedIn.value = it.isSuccessful
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