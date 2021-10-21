package com.example.talks.ui.authentication.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.talks.R
import com.example.talks.constants.LocalConstants
import com.example.talks.data.viewmodels.authentication.activity.MainActivityViewModel
import com.example.talks.databinding.FragmentSecondBinding
import com.example.talks.others.dialog.WaitingDialog
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
    private val viewModel: MainActivityViewModel by activityViewModels()

    // firebase auth instance
    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var prefs: SharedPreferences

    private var phoneNumber: String? = null
    private lateinit var dialog: WaitingDialog

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        binding.otpEditText.requestFocus()

        phoneNumber = prefs.getString("phoneNumber", "")
        binding.enteredPhoneNumber.text = phoneNumber?.formatForScreen() ?: "Number Not Found"

        dialog = WaitingDialog(requireActivity())

        // send verification code on given phone number
        viewModel.sendVerificationCode(phoneNumber, requireActivity(), auth)

        // start count down timer of 60s
        startCountdownForResendOTP(phoneNumber!!)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners() // all click listeners in this fragment

        subscribeToObservers() // all live data observers
    }

    @SuppressLint("LogNotTimber")
    private fun subscribeToObservers() {

        // firebase verification id for otp auth
        viewModel.verificationID.observe(viewLifecycleOwner, {
            if (it != null) {
                Log.d("verification id ===", it)
            }
        })

        // 6 digit sms code
        viewModel.smsCode.observe(viewLifecycleOwner, {
            Log.d("verification code ===", it ?: "no code")
            otpScreenProgressBar.visibility = View.VISIBLE
            binding.otpEditText.setText(it)
        })

        // once user is successfully authenticated
        viewModel.isUserLoggedIn.observe(viewLifecycleOwner, {
            if (it) {
                // Update authentication state in shared prefs
                prefs.edit()
                    .putInt(LocalConstants.KEY_AUTH_STATE, LocalConstants.AUTH_STATE_ADD_DATA)
                    .apply()
                dialog.dismiss()

                // navigate to third fragment
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_secondFragment_to_thirdFragment)
            } else {
                dialog.dismiss()
                showAlertDialogForIncorrectOtp()
            }
        })
    }

    private fun setClickListeners() {
        // OTP Handler
        binding.otpEditText.addTextChangedListener {
            if (it?.length == 6) {
                viewModel.manualOTPAuth(it.toString())
                otpScreenProgressBar.visibility = View.VISIBLE
            }
        }

        // Revert back to phone number entering screen i.e. First Fragment.
        binding.wrongNumberText.setOnClickListener {
            view?.let { it1 ->
                Navigation.findNavController(it1)
                    .navigate(R.id.action_secondFragment_to_firstFragment2)
            }
        }
    }

    private fun startCountdownForResendOTP(phoneNumber: String) {
        object : CountDownTimer(60000, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.resendOtpTextView.apply {
                    text = "Resend OTP in : " + millisUntilFinished / 1000 + "s"
                    isClickable = false
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {

                binding.resendOtpTextView.apply {
                    text = "Resend OTP"
                    isClickable = true
                    setOnClickListener {
                        viewModel.sendVerificationCode(phoneNumber, requireActivity(), auth)
                        start()
                    }
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

    private fun String.formatForScreen(): String {
        val number = this
        val formattedNumber = StringBuilder()
        formattedNumber.apply {
            append(number.substring(0..2))
            append(" ")
            append(number.substring(3..7))
            append(" ")
            append(number.substring(8))
        }
        return formattedNumber.toString()
    }
}
