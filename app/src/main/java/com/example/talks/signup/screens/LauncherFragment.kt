package com.example.talks.signup.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import com.example.talks.R


class LauncherFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_launcher, container, false)

        view.findViewById<Button>(R.id.continue_button).setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_firstFragment)
        }

        return view

    }
}