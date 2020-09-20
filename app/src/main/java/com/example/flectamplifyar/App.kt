package com.example.flectamplifyar

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.util.Log
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Marker
import com.example.flectamplifyar.ui.LoadMarkerSpinnerItemState
import com.example.flectamplifyar.ui.LoadMarkersSpinnerAdapter
import java.io.File
import java.util.*

class App : Application() {

    data class LocalMarker(val id:String, val score:Int, val name:String, val path:String, val owner:String, val bitmap:Bitmap)

    companion object {

        lateinit private var instance: App

        fun getApp(): App{ return instance }

        val isOnline: Boolean
            get() {
                val cm = getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }


        var currentMarker:Marker? = null
        fun setCurrentMarker(id:String){
            Amplify.API.query(
                ModelQuery.get(Marker::class.java, id),
                { response ->
                    Log.e("---------------------","RESPONSE!: ${response.data}")
                    if(response.data.canvases.size==0){
                        val canvas = Canvas.Builder()
                            .title("")
                            .owner("")
                            .id(UUID.randomUUID().toString())
                            .marker(response.data)
                            .build()

                        Amplify.API.mutate(
                            ModelMutation.create(canvas),
                            { response -> Log.i("MyAmplifyApp", "Create Canvas with id: " + response) },
                            { error -> Log.e("MyAmplifyApp", "Create Canvas failed", error) }
                        )
                    }
                },
                { error -> Log.e("MyAmplifyApp", "Query failure", error) }
            )
        }

    }

    var marker: Bitmap? = null


    init {
        instance = this
    }

}

