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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.talks.BuildConfig
import com.example.talks.R
import com.example.talks.database.ChatListItem
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.databinding.ActivityHomeScreenBinding
import com.example.talks.fileManager.FileManager
import com.example.talks.profile.ProfileSettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.novoda.merlin.Merlin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_calling.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class HomeScreenActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    private lateinit var bottomNavigationView: BottomNavigationView

    private val viewModel: HomeActivityViewModel by viewModels()
    private val databaseViewModel: TalksViewModel by viewModels()

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var auth: FirebaseAuth

    private val encryptionKey = BuildConfig.ENCRYPTION_KEY
    var contactNamesWithPhoneNumberAsKey = HashMap<String, String>()
    private var contactPhoneNumbers = ArrayList<String>()
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

        if (isPermissionGranted()) {
            readContacts()
        }
        viewModel.readMessagesFromServer(auth.currentUser?.phoneNumber, databaseViewModel)

        databaseViewModel.getChatListPhoneNumbers.observe(this, {
            chatChannelPhoneNumbers = it as ArrayList<String>
        })
        databaseViewModel.lastAddedMessage.observe(this, {
            if (it != null) {
                if (!chatChannelPhoneNumbers.contains(it.chatId)) {
                    val chatListItem =
                        ChatListItem(contactNumber = it.chatId, messageID = it.messageID)
                    databaseViewModel.createChatChannel(chatListItem)
                    Timber.d("${it.messageID} at creation====")
                } else {
                    Timber.d("${it.messageID} at update====")
                    databaseViewModel.updateChatChannel(
                        contact_number = it.chatId,
                        messageID = it.messageID.toString()
                    )
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
                    viewModel.getUsersFromServer(
                        it,
                        contactNamesWithPhoneNumberAsKey,
                        databaseViewModel,
                        encryptionKey
                    )
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
//        viewModel.getCurrentUserData(auth.currentUser?.uid, databaseViewModel)

        databaseViewModel.readAllUserData.observe(this, {
            val user1 = it[0]
            Glide.with(this).load(user1.profileImage)
                .placeholder(R.drawable.ic_baseline_person_24)
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
                        auth.currentUser?.uid.toString(),
                        contactNamesWithPhoneNumberAsKey
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
            contactPhoneNumbers.add(phoneNumber)
            contactNamesWithPhoneNumberAsKey[phoneNumber] = contactName
            val contact = TalksContact(
                phoneNumber, contactName
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