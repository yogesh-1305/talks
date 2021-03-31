package com.example.talks.home.profileScreen

import android.media.Image
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.database.UserViewModel
import com.example.talks.databinding.FragmentProfileBinding
import com.example.talks.encryption.Encryption
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.loader.ImageLoader
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var databaseViewModel: UserViewModel
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"
    var image = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        databaseViewModel.readAllUserData.observe(viewLifecycleOwner, {
            val user1 = it[0]
            image = Encryption().decrypt(user1.profileImage, encryptionKey).toString()
            Glide.with(this).load(image).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.profileScreenImage)
        })

        binding.profileScreenBackButton.setOnClickListener{
            activity?.finish()
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.contact_screen_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}