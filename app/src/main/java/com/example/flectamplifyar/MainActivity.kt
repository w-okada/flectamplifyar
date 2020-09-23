package com.example.flectamplifyar

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Element
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.amplify.AmplifyClient
import com.example.flectamplifyar.helper.FullScreenHelper
import com.example.flectamplifyar.model.StrokeProvider
import com.example.flectamplifyar.ui.ARFragment
import com.example.flectamplifyar.ui.ImageCaptureView
import com.example.flectamplifyar.ui.LoadMarkerSpinnerItemState
import com.example.flectamplifyar.ui.LoadMarkersSpinnerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(){
    companion object {
        private val TAG: String = MainActivity::class.java.getSimpleName()
    }

    var selectedMarkerBitmap: Bitmap? = null
    var selectedMarkerId: String? = null

    lateinit var user:UserProfile
    lateinit var amplify:AmplifyClient

    var currentMarker: Marker? =null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        user = UserProfile(UUID.randomUUID().toString())
        amplify = AmplifyClient(this)
        amplify.dbChnageListener = object:AmplifyClient.DBChangeListener{
            override fun canvasAdded(canvas: Canvas) {
            }

            override fun elementAdded(element: Element) {
            }

            override fun elementUpdated(element: Element) {
            }

        }

        (arFragment as ARFragment).arOperationListener = object:ARFragment.AROperationListener{
            override fun uploadMarker(bitmap: Bitmap, filename: String, displayName: String,
                                      onSuccess:((uploadId:String, score:Int) -> Unit), onError:((message:String) -> Unit)) {
                val exampleFile = File(App.getApp().applicationContext.filesDir, "${filename}")
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, exampleFile.outputStream())
                val mHandler = Handler(Looper.getMainLooper());
                val key = "pict/${filename}"
                Log.e(TAG, "Marker bitmap uploading... ${exampleFile}, ${key}")
                amplify.uploadFile(key, exampleFile,
                    {message->
                        Log.i(TAG, "Marker bitmap Successfully uploaded. Next, registering...: ${message}")
                        amplify.registerMarker(key, displayName,
                            {uploadId, score ->
                                onSuccess(uploadId, score)
                            },
                            {message ->
                                onError("register failed. ${message}")
                            }
                        )
                    },
                    {message->
                        onError("upload failed. ${message}")
                    }
                )
            }


            override fun setCurrentMarker(id: String, onSuccess:(message:String)-> Unit, onError:((message:String) -> Unit)) {
                amplify.queryMarker(id,
                    {marker, canvas ->
                        currentMarker = marker  // set current marker here
                        if(canvas!=null){
                            marker.canvases.add(canvas)
                        }
                        onSuccess("setCurrent Marker ${id} success. Marker:${marker}, Canvas:${canvas}")
                    },
                    {message ->
                        onError("setCurrent Marker ${id} failed. ${message}")
                    }
                )
            }

            override fun getMarkers(onSuccess: (markers: List<Marker>) -> Unit, onError: (message: String) -> Unit) {
                Log.e(TAG, "start get markers")
                amplify.queryMarkers(20,
                    {markers ->
                        onSuccess(markers)
                    },
                    {message ->
                        onError("get Markers failed. ${message}")
                    }
                )
            }

            override fun updateDB(uuid:String, json: String, add: Boolean, onSuccess: (message: String) -> Unit, onError: (message: String) -> Unit) {
                amplify.updateElement(user, currentMarker!!.canvases[0], uuid, json, add,
                    {message ->
                        Log.i(TAG, "updateDB success. ${message}")
                    },
                    {message ->
                        Log.e(TAG, "updateDB failed. ${message}")
                    },
                )
            }
        }

    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE) {
            Amplify.Auth.handleWebUISignInResponse(data)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

}