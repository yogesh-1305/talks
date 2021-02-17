package com.example.talks.home.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.talks.R
import com.example.talks.database.TalksContact
import com.example.talks.database.UserViewModel
import com.example.talks.encryption.Encryption
import com.google.android.material.navigation.NavigationView

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listener: NavController.OnDestinationChangedListener
    private val encryptionKey = "DB5583F3E615C496FC6AA1A5BEA33"

    private lateinit var viewModel: HomeActivityViewModel
    private var contacts = ArrayList<String>()

    private lateinit var databaseViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this).get(HomeActivityViewModel::class.java)
        databaseViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        databaseViewModel.readAllUserData.observe(this, {
            Log.i("database user===", it.toString())
        })
        databaseViewModel.readAllContacts.observe(this,{
            Log.i("contacts db===", it.toString())
        })

        if (isPermissionGranted()) {
            readContacts()
        }

        navController = findNavController(R.id.fragment_home_nav)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        listener =
            NavController.OnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.homeScreenFragment -> {
                        Toast.makeText(
                            applicationContext,
                            controller.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    R.id.contactScreenFragment -> {
                        Toast.makeText(
                            applicationContext,
                            controller.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    R.id.settingsFragment -> {
                        Toast.makeText(
                            applicationContext,
                            controller.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_home_nav)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()

        viewModel.getUsersFromServer(contacts)
        viewModel.users.observe(this, {
            val user = it
            for (data in user) {

                val number = data.getUserPhoneNumber()
                val name = data.getUserName()
                val image = Encryption().decrypt(data.getUserProfileImage(), encryptionKey)
                val uid = data.getUid()

                val contact = TalksContact(number, name, "$image", "$uid")
                databaseViewModel.addContact(contact)
            }
        })
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