package com.example.talks.signup.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.talks.R
import com.example.talks.database.TalksContact
import com.example.talks.database.TalksViewModel
import com.example.talks.home.activity.HomeScreenActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var databaseViewModel: TalksViewModel

    private var contactPhoneNumberList = ArrayList<String>()
    private var contactNameList = HashMap<String, String>()
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"
    private var dataFetchedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(TalksViewModel::class.java)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        /* If user is already logged in -> navigate to home screen
        * else ask fro permissions and start signup process*/
        if (isCurrentUserLoggedIn(auth)) {
            navigateToHomeScreenActivity()
        } else {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0 != null) {
                            if (p0.areAllPermissionsGranted()) {
                                readContacts()
                            } else {
                                readContacts()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                }).check()
        }

        databaseViewModel.readContactPhoneNumbers.observe(this, {
            if (it != null) {
                if (!dataFetchedOnce) {
                    viewModel.getUsersFromServer(it, contactNameList, databaseViewModel)
                    dataFetchedOnce = true
                }
            }
        })

    }

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
            contactPhoneNumberList.add(phoneNumber)
            contactNameList[phoneNumber] = contactName
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

    override fun onBackPressed() {
        super.onBackPressed()
        Log.i("back===", "pressed")
    }

    override fun onResume() {
        super.onResume()
        Log.i("===", " ")
    }

    private fun isCurrentUserLoggedIn(auth: FirebaseAuth): Boolean {
        return auth.currentUser != null
    }

    private fun navigateToHomeScreenActivity() {
        startActivity(Intent(applicationContext, HomeScreenActivity::class.java))
        finish()
    }

    private fun isContactPermissionGranted(): Boolean {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            readContacts()
        }
    }
}