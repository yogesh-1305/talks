package com.example.talks.networkManager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkManager(){

    fun isDataConnected(context: Context?): Boolean {
        val connectivityManager: ConnectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiConnected: NetworkInfo? =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val dataConnected: NetworkInfo? =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConnected != null && wifiConnected.isConnected || (dataConnected != null && dataConnected.isConnected)
    }

}