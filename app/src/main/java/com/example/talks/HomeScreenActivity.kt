package com.example.talks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.talks.R
import com.google.android.material.navigation.NavigationView

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

//        navController = findNavController(R.id.fragment_home_nav)
//        drawerLayout = findViewById(R.id.drawer_layout)
//        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
//
//        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
//        navigationView.setupWithNavController(navController)
//        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}