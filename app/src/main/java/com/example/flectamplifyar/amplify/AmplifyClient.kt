package com.example.flectamplifyar.amplify

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.ApiOperation
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.graphql.model.ModelSubscription
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Element
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.MainActivity
import com.example.flectamplifyar.R
import com.example.flectamplifyar.UserProfile
import com.example.flectamplifyar.ui.ARFragment
import com.example.flectamplifyar.ui.ImageCaptureView
import java.io.File
import java.util.*

class AmplifyClient(context: Context){
    companion object{
        val TAG = AmplifyClient::class.java.simpleName
    }
    var subscriptionForCanvasCreate: ApiOperation<*>? = null
    var subscriptionForElementCreate: ApiOperation<*>? = null
    var subscriptionForElementUpdate: ApiOperation<*>? = null

    interface DBChangeListener{
        fun canvasAdded(canvas:Canvas)
        fun elementAdded(element:Element)
        fun elementUpdated(element:Element)
    }

    var dbChnageListener:DBChangeListener? = null

    init{
        try {
            Log.i(TAG, "Initializing Amplify")

            val config = AmplifyConfiguration.builder(context)
                .devMenuEnabled(false)
                .build()
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(config, App.getApp().applicationContext)


            // 認証処理
            Amplify.Auth.fetchAuthSession(
                { result ->
                    Log.i(TAG, "Authentification success: ${result}")
                    if(result.isSignedIn == true){
                        setSubscription()
                    }else{
                        Amplify.Auth.signInWithWebUI(
                            context as Activity,
                            { result ->
                                Log.i(TAG, "Sign-in success ${result}" )
                                setSubscription()
                            },
                            { error ->
                                Log.e(TAG, "Sign-in failed ${error}")
                            }
                        )
                    }
                },
                { error ->
                    Log.i(TAG, "Authentification failed: ${error}")
                }
            )

        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify ${error.toString()}" )
        }
    }

    private fun setSubscription(){
        // サブスクリプション設定
        if(subscriptionForCanvasCreate == null){
            subscriptionForCanvasCreate = Amplify.API.subscribe(
                ModelSubscription.onCreate(Canvas::class.java),
                { Log.i(TAG, "Subscription for create cavans established") },
                { onCreated ->
                    Log.i(TAG, "Canvas create subscription received: " + onCreated.data)
                    dbChnageListener?.canvasAdded(onCreated.data)
//                    if(onCreated.data.marker.id == currentMarker?.id){
//                        currentMarker?.canvases?.add(onCreated.data)
//                        Log.e(ARFragment.TAG, "match current marker ${currentMarker!!.canvases.size}")
//                    }else{
//                        Log.e(ARFragment.TAG, "not match current marker ${currentMarker!!.id}, ${onCreated.data.marker.id}")
//                    }
                },
                { onFailure ->
                    Log.e(TAG, "Subscription for create canvas failed", onFailure)
                },
                { Log.i(TAG, "Subscription for create canvas completed") }
            )
        }

        if(subscriptionForElementCreate == null){
            subscriptionForElementCreate = Amplify.API.subscribe(
                ModelSubscription.onCreate(Element::class.java),
                { Log.i(TAG, "Subscription for create elements established") },
                { onCreated ->
                    Log.i(TAG, "Element create subscription received: " + onCreated.data)
                    dbChnageListener?.elementAdded(onCreated.data)
                },
                { onFailure ->
                    Log.e(TAG, "Subscription for create element failed", onFailure)
                },
                { Log.i(TAG, "Subscription for create element completed") }
            )
        }

        if(subscriptionForElementUpdate == null){
            subscriptionForElementUpdate = Amplify.API.subscribe(
                ModelSubscription.onUpdate(Element::class.java),
                { Log.i(TAG, "Subscription for update elements established") },
                { onUpdated ->
                    Log.i(TAG, "Element update subscription received: " + onUpdated.data)
                    dbChnageListener?.elementUpdated(onUpdated.data)
                },
                { onFailure ->
                    Log.e(TAG, "Subscription for update element failed", onFailure)
                },
                { Log.i(TAG, "Subscription for update element completed") }
            )
        }
    }


    fun uploadFile(key:String, exampleFile: File, onSuccess:((message:String) -> Unit), onError:((message:String) -> Unit)){
        Amplify.Storage.uploadFile(
            key,
            exampleFile,
            { result ->
                onSuccess("Fileupload success: ${result}")
            },
            { error ->
                onError("Fileupload error. ${error}")
            }
        )
    }

    fun registerMarker(key:String, title: String, onSuccess:((uploadId:String, score:Int) -> Unit), onError:((message:String) -> Unit)){
        try {
            // POSTで画像を登録。
            val plugin = Amplify.Storage.getPlugin("awsS3StoragePlugin") as AWSS3StoragePlugin
            val body = "{" +
                    "\"bucket\":\"${plugin.bucketName}\", " +
                    "\"region\":\"${plugin.regionStr}\", " +
                    "\"key\":\"${key}\", " +
                    "\"name\":\"${title}\" " +
                    "}"
            Log.e(TAG, "regsitering.... POST BODY -> ${body}")
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
    }

    fun queryMarker(id:String, onSuccess:(marker:Marker, canvas:Canvas?)-> Unit, onError:((message:String) -> Unit)){
        Amplify.API.query(
            ModelQuery.get(Marker::class.java, id),
            { response ->
                Log.e(TAG,"Query Markers response${id}: ${response.data}")
                // 当該Markerが見つからない場合
                if(response.data == null){
                    onError("Marker not found! ${id}")
                    return@query
                }
                val marker = response.data
                // Canvasが一つもない場合は追加。
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
                            onSuccess(marker, response.data) // Canvas追加して成功
                        },
                        { error ->
                            onError("Marker[${id}] first canvas create failed ${error}")
                        }
                    )
                }else{
                    onSuccess(marker, null) // Canvas追加せずに成功
                }
            },
            { error ->
                onError("Marker[${id}] query failed ${error}")
            }
        )
    }


    fun queryMarkers(scoreLimit:Int, onSuccess: (markers: List<Marker>) -> Unit, onError: (message: String) -> Unit){
        Amplify.API.query(
            ModelQuery.list(Marker::class.java, Marker.SCORE.gt(scoreLimit)),
            { response ->
                val tmpList = mutableListOf<Marker>()
                Log.e(TAG, "LIST MARKER[all]: ${response}")
                for (marker in response.data) {
                    tmpList.add(marker)
                    Log.e(TAG, "LIST MARKER[Marker]: ${marker}, ${marker.canvases.size}")
                    for (canvas in marker.canvases) {
                        Log.e(TAG, "LIST MARKER[Canvas]: ${canvas}")
                        for (element in canvas.elements) {
                            Log.e(TAG, "LIST MARKER[Element]: ${element}")
                        }
                    }
                }
                onSuccess(tmpList)
            },
            { error ->
                onError("query markers failed. ${error}")
            }
        )
    }

    fun updateElement(user:UserProfile, canvas:Canvas, uuid:String, json:String, add:Boolean,
                      onSuccess:(message: String) -> Unit, onError: (message: String) -> Unit){
        try {
            val element = Element.builder()
                .owner(user.uuid)
                .content(json)
                .id(uuid)
                .canvas(canvas)
                .build()
            if (add == true) {
                Amplify.API.mutate(
                    ModelMutation.create(element),
                    { response ->
                        onSuccess("Create element with id: " + response)
                    },
                    { error ->
                        onError("Create element failed ${error}")
                    }
                )
            } else {
                Amplify.API.mutate(
                    ModelMutation.update(element),
                    { response ->
                        onSuccess("Updated element with id: ${response}")
                    },
                    { error ->
                        onError( "Update element failed")
                    }
                )
            }
        }catch(e:Exception){
            onError( "Update element failed ${e}")
        }
    }

}