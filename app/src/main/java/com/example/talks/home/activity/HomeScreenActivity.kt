package com.example.talks.home.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.talks.R
import com.example.talks.database.ChatListItem
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityHomeScreenBinding
import com.example.talks.encryption.Encryption
import com.example.talks.fileManager.FileManager
import com.example.talks.profile.ProfileSettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.novoda.merlin.Merlin
import kotlinx.android.synthetic.main.activity_calling.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeScreenActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var viewModel: HomeActivityViewModel
    private lateinit var databaseViewModel: TalksViewModel

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var auth: FirebaseAuth

    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"
    var contactList = HashMap<String, String>()
    private var contacts = ArrayList<String>()
    private var chatChannelPhoneNumbers = ArrayList<String>()
    private var serverFetchExecuted = false

    private lateinit var merlin: Merlin

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        FileManager().createDirectoryInExternalStorage()
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(HomeActivityViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)

        if (isPermissionGranted()) {
            readContacts()
        }
        for (number in contactList.keys) {
            databaseViewModel.updateChatChannelUserName(number)
        }

        viewModel.readMessagesFromServer(auth.currentUser?.phoneNumber, databaseViewModel)

        databaseViewModel.chatChannels.observe(this, {
            chatChannelPhoneNumbers = it as ArrayList<String>
        })

        databaseViewModel.lastAddedMessage.observe(this, { message ->
            if (message != null) {
                if (chatChannelPhoneNumbers.contains(message.chatId)) {
                    // update the existing chat channel with last message and timestamp
                    databaseViewModel.updateChatChannel(
                        message.messageText.toString(), message.sendTime.toString(), "text",
                        message.chatId
                    )
                } else {
                    // create new chat channel
                    val chatChannel =
                        ChatListItem(
                            message.chatId,
                            contactList[message.chatId],
                            message.messageText,
                            "text",
                            message.sendTime.toString()
                        )
                    databaseViewModel.createChatChannel(chatChannel)
                    databaseViewModel.updateChatChannelUserName(message.chatId)
                }
            }
        })

        bottomNavigationView = binding.homeBottomNav
        navController = findNavController(R.id.fragment_home_nav)
        bottomNavigationView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeScreenFragment,
                R.id.videoRoomFragment,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        databaseViewModel.readContactPhoneNumbers.observe(this, {
            if (it != null) {
                if (!serverFetchExecuted) {
                    viewModel.getUsersFromServer(it, contactList, databaseViewModel, encryptionKey)
                    Toast.makeText(this, "loop check2", Toast.LENGTH_SHORT).show()
                    serverFetchExecuted = true
                }
            }
        })

        destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->

                if (destination.id == R.id.homeScreenFragment) {
                    binding.toolbarUsername.text = "Talks"
                } else if (destination.id == R.id.videoRoomFragment) {
                    binding.toolbarUsername.text = "Room"
                }

            }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getCurrentUserData(auth.currentUser?.uid, databaseViewModel)

        databaseViewModel.readAllUserData.observe(this, {
            val user1 = it[0]
            val image1 = Encryption().decrypt(user1.profileImage, encryptionKey)
//            Log.i("TAG IMAGE=====", image1.toString())

            Glide.with(this).load(image1).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.toolbarDP)
        })

        binding.toolbarDP.setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

        // works when network is connected
        merlin = Merlin.Builder().withConnectableCallbacks()
            .build(this).apply {
                registerConnectable {
                    viewModel.readPeerConnections(
                        this@HomeScreenActivity,
                        auth.currentUser?.uid.toString()
                    )
                }
            }
    }

    override fun onResume() {
        merlin.bind()
        navController.addOnDestinationChangedListener(destinationChangedListener)
        super.onResume()
    }

    override fun onPause() {

        navController.removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }


    @SuppressLint("Recycle")
    private fun readContacts() {
        val phones = this.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        if (phones != null) {
            showContacts(phones)
        }
    }

    private fun showContacts(phones: Cursor) {
        while (phones.moveToNext()) {
            val contactName =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phoneNumber = phoneNumber.replace("\\s".toRegex(), "").trim()
            phoneNumber = formatPhoneNumber(phoneNumber)
            contacts.add(phoneNumber)
            contactList[phoneNumber] = contactName
            val contact = TalksContact(
                phoneNumber, null, contactName, null,
                null, null, null, null, null
            )
            databaseViewModel.addContact(contact)
        }

    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        var number = phoneNumber
        if (!number.startsWith("+")) {
            number = if (phoneNumber.startsWith("0")) {
                number = phoneNumber.drop(1)
                "+91$number"
            } else {
                "+91$number"
            }
        }
        return number
    }

    private fun isPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                Array(1) { Manifest.permission.READ_CONTACTS },
                111
            )
            false
        } else {
            true
        }
    }
}