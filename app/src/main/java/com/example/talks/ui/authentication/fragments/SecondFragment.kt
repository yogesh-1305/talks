package com.example.talks.ui.authentication.fragments

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.talks.R
import com.example.talks.data.viewmodels.authentication.fragments.SecondFragmentViewModel
import com.example.talks.databinding.FragmentSecondBinding
import com.example.talks.others.Constants
import com.example.talks.others.dialog.WaitingDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_second.*
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
@AndroidEntryPoint
class SecondFragment : Fragment() {

    // view binding instance
    private lateinit var binding: FragmentSecondBinding

    // view model instance
    private lateinit var viewModel: SecondFragmentViewModel

    // firebase auth instance
    private lateinit var auth: FirebaseAuth

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var otpTextView: OtpTextView
    private lateinit var resendOTPTextView: TextView
    private var phoneNumber: String? = null
    private lateinit var dialog: WaitingDialog

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SecondFragmentViewModel::class.java)

        phoneNumber = prefs.getString("phoneNumber", "")
        val waitingText = binding.waitingInstructionsTextView
        waitingText.text =
            "Waiting to automatically detect an SMS sent to \'$phoneNumber\'"
        otpTextView = binding.otpView

        dialog = activity?.let { WaitingDialog(it) }!!

        // Firebase initialization
//        context?.let { FirebaseApp.initializeApp(it) }
        auth = FirebaseAuth.getInstance()


        viewModel.sendVerificationCode(phoneNumber, requireActivity(), auth)

        viewModel.verificationID.observe(viewLifecycleOwner, {
            if (it != null) {
                otpScreenProgressBar.visibility = View.VISIBLE
            }
        })

        viewModel.smsCode.observe(viewLifecycleOwner, {
            otpTextView.otp = it
        })

        // OTP Handler
        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // do nothing
            }

            override fun onOTPComplete(otp: String?) {
                viewModel.manualOTPAuth(otp)
                otpScreenProgressBar.visibility = View.VISIBLE
            }
        }

        // Resend OTP
        resendOTPTextView = binding.resendOtpTextView
        startCountdownForResendOTP(phoneNumber!!)

        // Revert back to phone number entering screen i.e. First Fragment.
        binding.wrongNumberText.setOnClickListener {
            view?.let { it1 ->
                Navigation.findNavController(it1)
                    .navigate(R.id.action_secondFragment_to_firstFragment2)
            }
        }

        viewModel.isUserLoggedIn.observe(viewLifecycleOwner, {
            if (it) {

                prefs.edit().putInt(Constants.KEY_AUTH_STATE, Constants.AUTH_STATE_ADD_DATA).apply()
                dialog.dismiss()
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_secondFragment_to_thirdFragment)
            } else {
                dialog.dismiss()
                showAlertDialogForIncorrectOtp()
            }
        })

        return binding.root
    }

    private fun startCountdownForResendOTP(phoneNumber: String) {
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
                    viewModel.sendVerificationCode(phoneNumber, requireActivity(), auth)
                    start()
                }
            }
        }.start()
    }

    private fun showAlertDialogForIncorrectOtp() {
        otpScreenProgressBar.visibility = View.INVISIBLE
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("OOPS! Incorrect OTP.")
        dialog.setMessage("Maybe you've mistaken entering the correct OTP. ")
        dialog.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog.show()
    }
}
