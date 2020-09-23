package com.example.flectamplifyar

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.Log
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Element
import com.amplifyframework.datastore.generated.model.Marker
import com.example.flectamplifyar.dbmodel.DBObject
import com.example.flectamplifyar.ui.LoadMarkerSpinnerItemState
import com.example.flectamplifyar.ui.LoadMarkersSpinnerAdapter
import com.google.gson.Gson
import java.io.File
import java.util.*
import javax.vecmath.Vector3f

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

