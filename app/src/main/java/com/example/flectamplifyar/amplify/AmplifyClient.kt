package com.example.flectamplifyar.amplify

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.ApiOperation
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelSubscription
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Element
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import com.example.flectamplifyar.ui.ARFragment

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
                { onCreated ->
                    Log.i(TAG, "Element update subscription received: " + onCreated.data)
                },
                { onFailure ->
                    Log.e(TAG, "Subscription for update element failed", onFailure)
                },
                { Log.i(TAG, "Subscription for update element completed") }
            )
        }
    }


}