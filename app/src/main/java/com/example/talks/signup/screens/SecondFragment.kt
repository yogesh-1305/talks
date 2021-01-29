package com.example.talks.signup.screens

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.talks.R
import com.example.talks.database.User
import com.example.talks.database.UserViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@Suppress("NAME_SHADOWING")
class SecondFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val args: SecondFragmentArgs by navArgs()
    private lateinit var otpTextView: OtpTextView
    private var storedVerificationId = ""
    private lateinit var resendOTPTextView: TextView
    private lateinit var phoneNumber: String
    private lateinit var userViewModel: UserViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        otpTextView = view.findViewById(R.id.otpView)
        val waitingText = view.findViewById<TextView>(R.id.waiting_instructions_text_view)
        context?.let { FirebaseApp.initializeApp(it) }
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // retrieving phone number string from First Fragment
        phoneNumber = args.phoneNumber
        waitingText.text = "Waiting to automatically detect an SMS sent to \'$phoneNumber\'"

        lifecycleScope.launch(Dispatchers.IO){
            sendVerificationCode(phoneNumber)
        }

        // OTP Handler
        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // do nothing
            }
            override fun onOTPComplete(otp: String?) {
                if (storedVerificationId == "") {
                    showAlertDialogForIncorrectOtp()
                } else {
                    val credential =
                        PhoneAuthProvider.getCredential(storedVerificationId, otp.toString())
                    signInWithPhoneAuthCredentials(credential)
                }
            }
        }

        // Resend OTP
        resendOTPTextView = view.findViewById(R.id.resend_otp_text_view)
        startCountdownForResendOTP(phoneNumber)

        // Revert back to phone number entering screen i.e. First Fragment.
        view.findViewById<TextView>(R.id.wrong_number_text).setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_secondFragment_to_firstFragment2)
        }

        return view
    }

    private fun startCountdownForResendOTP(phoneNumber: String){
        object : CountDownTimer(60000, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                resendOTPTextView.text = "Resend OTP in : " + millisUntilFinished / 1000 + "s"
                resendOTPTextView.isClickable = false
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                resendOTPTextView.text = "Resend OTP"
                resendOTPTextView.isClickable = true
                resendOTPTextView.setOnClickListener {
                    sendVerificationCode(phoneNumber)
                    start()
                }
            }

        }.start()

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
            Toast.makeText(activity, p0.localizedMessage, Toast.LENGTH_SHORT).show()
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
        auth.signInWithCredential(p0)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("sign in success-----", it.toString())
                    val user = User(0, phoneNumber, "Hello New_User", " ")
                    addUserToLocalDatabase(user)
                    view?.let { it1 ->
                        Navigation.findNavController(it1)
                            .navigate(R.id.action_secondFragment_to_confirmation_screen)
                    }
                } else {
                    Log.i("sign in failed-----", it.toString())
                    showAlertDialogForIncorrectOtp()

                }
            }
    }

    private fun addUserToLocalDatabase(user: User){
        userViewModel.addUser(user)
        Log.i("database-------", user.phoneNumber)

    }

    private fun showAlertDialogForIncorrectOtp(){
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("OOPS! Incorrect OTP.")
        dialog.setMessage("Maybe you've mistaken entering the correct OTP. ")
        dialog.setPositiveButton("OK"){ dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }
}
