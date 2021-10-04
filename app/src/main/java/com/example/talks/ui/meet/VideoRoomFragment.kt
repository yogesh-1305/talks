package com.example.talks.ui.meet

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talks.R
import com.example.talks.data.viewmodels.contact.activity.ContactViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.FragmentVideoRoomBinding
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.MalformedURLException
import java.net.URL


class VideoRoomFragment : Fragment() {

    private lateinit var binding: FragmentVideoRoomBinding
    private lateinit var viewModel: ContactViewModel
    private lateinit var databaseViewModel: TalksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoRoomBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        try {
            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL("https://meet.jit.si"))
                .setAudioMuted(false)
                .setVideoMuted(false)
                .setAudioOnly(false)
                .setWelcomePageEnabled(false)
                .build()
            JitsiMeet.setDefaultConferenceOptions(options)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        binding.button2.setOnClickListener {
            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL("https://meet.jit.si"))
                .setRoom("room123")
                .setAudioMuted(false)
                .setVideoMuted(false)
                .setAudioOnly(false)
                .setWelcomePageEnabled(false)
                .build()
            JitsiMeetActivity.launch(activity, options)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contact_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}