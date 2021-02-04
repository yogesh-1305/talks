package com.example.talks.signup.screens.secondFragment

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.talks.R
import com.example.talks.databinding.FragmentSecondBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


@Suppress("NAME_SHADOWING")
class SecondFragment : Fragment() {

    private lateinit var binding: FragmentSecondBinding
    private lateinit var viewModel: SecondFragmentViewModel

    private lateinit var auth: FirebaseAuth
    private val args: SecondFragmentArgs by navArgs()
    private lateinit var otpTextView: OtpTextView
    private lateinit var resendOTPTextView: TextView
    private lateinit var phoneNumber: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SecondFragmentViewModel::class.java)

        // views
        otpTextView = binding.otpView
        val waitingText = binding.waitingInstructionsTextView


        // Firebase initialization
        context?.let { FirebaseApp.initializeApp(it) }
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")

        // retrieving phone number string from First Fragment
        phoneNumber = args.phoneNumber
        waitingText.text = "Waiting to automatically detect an SMS sent to \'$phoneNumber\'"

        context?.let {
            viewModel.sendVerificationCode(phoneNumber, it, auth, binding.root)
        }

        // OTP Handler
        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // do nothing
            }
            override fun onOTPComplete(otp: String?) {
                viewModel.otpAuth(otp, otpTextView)
            }
        }

        // Resend OTP
        resendOTPTextView = binding.resendOtpTextView
        startCountdownForResendOTP(phoneNumber)

        // Revert back to phone number entering screen i.e. First Fragment.
        binding.wrongNumberText.setOnClickListener {
            view?.let { it1 ->
                Navigation.findNavController(it1)
                    .navigate(R.id.action_secondFragment_to_firstFragment2)
            }
        }

        return binding.root
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
                    context?.let {
                            it1 -> viewModel.sendVerificationCode(phoneNumber, it1, auth, binding.root)
                    }
                    start()
                }
            }
        }.start()
    }

}
