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
                Amplify.Storage.uploadFile(
                    key,
                    exampleFile,
                    { result ->
                        Log.i(TAG, "Marker bitmap Successfully uploaded. Next, registering...: ${result}")
                        try {
                            // POSTで画像を登録。
                            val plugin = Amplify.Storage.getPlugin("awsS3StoragePlugin") as AWSS3StoragePlugin
                            val body = "{" +
                                    "\"bucket\":\"${plugin.bucketName}\", " +
                                    "\"region\":\"${plugin.regionStr}\", " +
                                    "\"key\":\"${key}\", " +
                                    "\"name\":\"${title}\" " +
                                    "}"
                            Log.e(ImageCaptureView.TAG, "regsitering.... POST BODY -> ${body}")
                            val options: RestOptions = RestOptions.builder()
                                .addPath("/markers")
                                .addBody(body.toByteArray())
                                .build()

                            Amplify.API.post(options,
                                { response ->
                                    Log.i(TAG, "POST response -> " + response.data.asString())
                                    val score = response.data.asJSONObject()["score"].toString().toIntOrNull()
                                    val uploadId = response.data.asJSONObject()["id"] as String


                                    // Scoreの有無で成否を判定
                                    if(score == null){
                                        onError("can not get enough feature from image ${score}")
                                    }else{
                                        onSuccess(uploadId, score)
                                    }

                                },
                                { error ->
                                    onError("analyzing image failed")
                                }
                            )
                        } catch (e: Exception) {
                            onError("upload failed")
                        }

                    },
                    { error -> onError("upload failed") }
                )
            }

            override fun setCurrentMarker(id: String,
                                          onSuccess:(message:String)-> Unit, onError:((message:String) -> Unit)) {
                Amplify.API.query(
                    ModelQuery.get(Marker::class.java, id),
                    { response ->
                        Log.e(TAG,"Query Markers response${id}: ${response.data}")
                        if(response.data == null){
                            onError("Marker not found! ${id}")
                            return@query
                        }
                        val marker = response.data
                        currentMarker = marker
                        if(marker.canvases.size==0){
                            val canvas = Canvas.Builder()
                                .title("")
                                .owner("")
                                .id(UUID.randomUUID().toString())
                                .marker(marker)
                                .build()

                            Amplify.API.mutate(
                                ModelMutation.create(canvas),
                                { response ->
                                    onSuccess("Marker[${id}] first canvas created ${response}")
                                },
                                { error ->
                                    onError("Marker[${id}] first canvas create failed ${error}")
                                }
                            )
                        }
                    },
                    { error ->
                        onError("Marker[${id}] query failed ${error}")
                    }
                )

            }

            override fun getMarkers(onSuccess: (markers: List<Marker>) -> Unit, onError: (message: String) -> Unit) {

                Log.e(TAG, "start get markers")
                Amplify.API.query(
                    ModelQuery.list(Marker::class.java, Marker.SCORE.gt(20)),
                    { response ->
                        val tmpList = mutableListOf<Marker>()
                        Log.e(TAG, "LIST MARKER[all]: ${response}")
                        for (marker in response.data) {
                            tmpList.add(marker)
                            Log.e(TAG, "LIST MARKER[Marker]: ${marker}, ${marker.canvases.size}")
                            for (canvas in marker.canvases) {
                                Log.e(TAG, "LIST MARKER[Canvas]: ${canvas}")
                                for (element in canvas.elements) {
                                    Log.e("---", "LIST MARKER[Element]: ${element}")
                                }
                            }
                        }
                        onSuccess(tmpList)
                    },
                    { error ->
                        onError("Query Marker failed. ${error}")
                    }
                )

            }

            override fun updateDB(uuid:String, json: String, add: Boolean, onSuccess: (message: String) -> Unit, onError: (message: String) -> Unit) {
                try {
                    val element = Element.builder()
                        .owner("owner")
                        .content(json)
                        .id(uuid)
                        .canvas(currentMarker!!.canvases[0])
                        .build()
                    if (add == true) {
                        Amplify.API.mutate(
                            ModelMutation.create(element),
                            { response -> Log.i(TAG, "Create element with id: " + response) },
                            { error -> Log.e(TAG, "Create element failed", error) }
                        )
                    } else {
                        Amplify.API.mutate(
                            ModelMutation.update(element),
                            { response -> Log.i("MyAmplifyApp", "Updated element with id: " + response) },
                            { error -> Log.e("MyAmplifyApp", "Update element failed", error) }
                        )
                    }
                }catch(e:Exception){
                    Log.e(TAG, "${e}")
                    Log.e(TAG, "CurrentMarker: ${currentMarker}")
                    Log.e(TAG, "CureentCanvs: ${currentMarker!!.canvases}")

                }
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