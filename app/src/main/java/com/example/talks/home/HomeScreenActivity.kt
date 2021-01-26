package com.example.talks.home

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.talks.R
import com.google.android.material.navigation.NavigationView

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listener : NavController.OnDestinationChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.fragment_home_nav)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        listener = NavController.OnDestinationChangedListener{controller, destination, arguments ->
            when (destination.id) {
                R.id.homeScreenFragment -> {
                    Toast.makeText(applicationContext, controller.toString(), Toast.LENGTH_SHORT).show()
                }
                R.id.contactScreenFragment -> {
                    Toast.makeText(applicationContext, controller.toString(), Toast.LENGTH_SHORT).show()
                }
                R.id.settingsFragment -> {
                    Toast.makeText(applicationContext, controller.toString(), Toast.LENGTH_SHORT).show()
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
}