package com.example.talks.signup.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.talks.R

class ConfirmationScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirmation_screen, container, false)

        view.findViewById<Button>(R.id.continue_button_on_dialog).setOnClickListener{
            updateUI()
        }

        return view
    }

    private fun updateUI(){
        val intent = Intent(context, HomeScreenActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


}