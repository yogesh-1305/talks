package com.example.talks.ui.authentication.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.talks.BuildConfig
import com.example.talks.R
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_DATA
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_NUMBER
import com.example.talks.constants.LocalConstants.AUTH_STATE_ADD_OTP
import com.example.talks.constants.LocalConstants.AUTH_STATE_COMPLETE
import com.example.talks.constants.LocalConstants.AUTH_STATE_FINAL_SETUP
import com.example.talks.constants.LocalConstants.KEY_AUTH_STATE
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.authentication.activity.MainActivityViewModel
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.databinding.ActivityMainBinding
import com.example.talks.others.utility.PermissionsUtility
import com.example.talks.ui.home.activity.HomeScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    private var screenState: ScreenState = ScreenState.AT_WELCOME_SCREEN

    private val talksViewModel: TalksViewModel by viewModels()
    private val viewModel: MainActivityViewModel by viewModels()

    private var dataFetchedOnce = false

    //encryption key (v.v.imp)
    private val encryptionKey = BuildConfig.ENCRYPTION_KEY

    @Inject
    lateinit var prefs: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavController()
        authScreenStateHandler()

        registerForPermissionCallbacks()

        lifecycleScope.launch {
            talksViewModel.readContactPhoneNumbers.observe(this@MainActivity, {
                if (it != null) {
                    if (!dataFetchedOnce) {
                        viewModel.getUsersFromServer(
                            it,
                            contactNameList,
                            talksViewModel,
                            encryptionKey
                        )
                        dataFetchedOnce = true
                    }
                }
            })
        }

    }

    @DelicateCoroutinesApi
    private fun setupNavController() {
        navController = findNavController(R.id.auth_activity_nav_host)
        destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.welcomeFragment -> {
                        screenState = ScreenState.AT_WELCOME_SCREEN
                    }
                    R.id.firstFragment -> {
                        screenState = ScreenState.AT_PHONE_SCREEN
                    }
                    R.id.secondFragment -> {
                        screenState = ScreenState.AT_VERIFICATION_SCREEN
                    }
                    R.id.thirdFragment -> {
                        screenState = ScreenState.AT_DATA_SCREEN
                        GlobalScope.launch {
                            delay(500)
                            requestContactsPermission()
                        }
                    }
                    R.id.finalSetupFragment -> {
                        screenState = ScreenState.PROCESS_COMPLETED
                    }
                }
            }
    }

    private fun authScreenStateHandler() {

        when (prefs.getInt(KEY_AUTH_STATE, 0)) {

            AUTH_STATE_COMPLETE -> {
                startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
                finish()
            }
            AUTH_STATE_ADD_DATA, AUTH_STATE_FINAL_SETUP -> {
                findNavController(R.id.auth_activity_nav_host).navigate(R.id.thirdFragment)
            }
            AUTH_STATE_ADD_OTP, AUTH_STATE_ADD_NUMBER -> {
                findNavController(R.id.auth_activity_nav_host).navigate(R.id.firstFragment)
            }
            0 -> { /* NO_OP */ }
        }
    }

    private fun requestContactsPermission() {
        if (PermissionsUtility.hasContactsPermissions(this)) return
        else contactPermission.launch(Manifest.permission.READ_CONTACTS)
    }


    private lateinit var contactPermission: ActivityResultLauncher<String>
    private fun registerForPermissionCallbacks() {
        contactPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    readContacts()
                } else {
                    AppSettingsDialog.Builder(this)
                        .setRationale("Talks require contacts permission to work properly")
                        .build().show()
                }
            }
    }

    private fun readContacts() {
        val phones = contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        if (phones != null) {
            showContacts(phones)
        }
    }

    private var contactPhoneNumberList = ArrayList<String>()
    private var contactNameList = HashMap<String, String>()
    @SuppressLint("Range")
    private fun showContacts(phones: Cursor) {
        lifecycleScope.launch {
            while (phones.moveToNext()) {
                val contactName =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var phoneNumber =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                phoneNumber = phoneNumber.replace("\\s".toRegex(), "").trim()
                phoneNumber = formatPhoneNumber(phoneNumber)
                contactPhoneNumberList.add(phoneNumber)
                contactNameList[phoneNumber] = contactName
                val contact = TalksContact(
                    phoneNumber, contactName
                )
                talksViewModel.addContact(contact)
            }
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

    override fun onResume() {
        navController.addOnDestinationChangedListener(destinationChangedListener)
        super.onResume()
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }

    override fun onBackPressed() {
        when (screenState) {
            ScreenState.AT_WELCOME_SCREEN,
            ScreenState.AT_PHONE_SCREEN -> finish()

            ScreenState.AT_VERIFICATION_SCREEN,
            ScreenState.AT_DATA_SCREEN -> moveTaskToBack(true)
            else -> { /* NO-OP */ }
        }

    }
}

enum class ScreenState {
    AT_WELCOME_SCREEN,
    AT_PHONE_SCREEN,
    AT_VERIFICATION_SCREEN,
    AT_DATA_SCREEN,
    PROCESS_COMPLETED

}