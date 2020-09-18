package com.example.flectamplifyar

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager

class App : Application() {
    companion object {
        lateinit private var instance: App

        fun getApp(): App{ return instance }

        val isOnline: Boolean
            get() {
                val cm = getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }
    }

    init {
        instance = this
    }

}

