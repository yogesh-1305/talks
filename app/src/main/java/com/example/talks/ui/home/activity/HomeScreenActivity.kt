package com.example.talks.ui.home.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.talks.BuildConfig
import com.example.talks.R
import com.example.talks.constants.LocalConstants
import com.example.talks.data.model.ChatListItem
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.ActivityHomeScreenBinding
import com.example.talks.data.viewmodels.home.activity.HomeActivityViewModel
import com.example.talks.others.utility.ExtensionFunctions.gone
import com.example.talks.ui.authentication.activity.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.novoda.merlin.Merlin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class HomeScreenActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    private lateinit var bottomNavigationView: BottomNavigationView

    private val viewModel: HomeActivityViewModel by viewModels()
    private val databaseViewModel: TalksViewModel by viewModels()

    private lateinit var binding: ActivityHomeScreenBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val encryptionKey = BuildConfig.ENCRYPTION_KEY
    var contactNamesWithPhoneNumberAsKey = HashMap<String, String>()
    private var contactPhoneNumbers = ArrayList<String>()
    private var chatChannelPhoneNumbers = ArrayList<String>()
    private var serverFetchExecuted = false

    private lateinit var merlin: Merlin

    @Inject
    lateinit var prefs: SharedPreferences

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("home activity starts ===", LocalDateTime.now().toString())

        // if user is not logged in -> go to login activity
        if (prefs.getInt(LocalConstants.KEY_AUTH_STATE, 0) == 0) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        clickListeners()

        setFragmentDestinationChangeListener()



        // works when network is connected
        merlin = Merlin.Builder().withConnectableCallbacks()
            .build(this).apply {
                registerConnectable {
//                    viewModel.readPeerConnections(
//                        this@HomeScreenActivity,
//                        auth.currentUser?.uid.toString(),
//                        contactNamesWithPhoneNumberAsKey
//                    )
                }
            }

        if (isPermissionGranted()) {
            readContacts()
        }

        viewModel.readMessagesFromServer(databaseViewModel)

        databaseViewModel.getChatListPhoneNumbers.observe(this, {
            chatChannelPhoneNumbers = it as ArrayList<String>
        })
        databaseViewModel.lastAddedMessage.observe(this, { latestMessage ->
            latestMessage?.let { message ->
                if (!chatChannelPhoneNumbers.contains(message.chatID)) {
                    val chatListItem =
                        ChatListItem(contactNumber = message.chatID, latestMessageId = message.id)
                    databaseViewModel.createChatChannel(chatListItem)

                } else {
                    databaseViewModel.updateChatListLatestMessage(
                        contact_number = message.chatID.toString(),
                        latestMessageId = message.id
                    )
                }
            }
        })

        bottomNavigationView = binding.homeBottomNav
        navController = findNavController(R.id.fragment_home_nav)
        bottomNavigationView.setupWithNavController(navController)
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.homeScreenFragment,
//                R.id.videoRoomFragment,
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)

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
    }

    private fun setFragmentDestinationChangeListener() {
        destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeScreenFragment -> {
                        fab_home_activity.show()
                    }
                    R.id.chatFragment -> {
                        fab_home_activity.gone()
                    }
                    R.id.contactsFragment -> {
                        fab_home_activity.gone()
                    }
                }
            }
    }

    private fun clickListeners() {
        fab_home_activity.setOnClickListener {
            findNavController(R.id.fragment_home_nav)
                .navigate(R.id.action_homeScreenFragment_to_contactsFragment)
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