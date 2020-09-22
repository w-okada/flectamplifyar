package com.example.flectamplifyar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.dbmodel.DBObject
import com.example.flectamplifyar.helper.FullScreenHelper
import com.example.flectamplifyar.ui.ARFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import javax.vecmath.Vector3f


class MainActivity : AppCompatActivity(){
    companion object {
        private val TAG: String = MainActivity::class.java.getSimpleName()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        val locations :Array<Vector3f> = arrayOf(Vector3f(3f,4f,5f))
//
//        val dbobj = DBObject(DBObject.TYPE.LINE, DBObject.TEXTURE_TYPE.COLOR, Color.RED, locations)
//
//        val json = Gson().toJson(dbobj)
//        Log.e(TAG, "JSON!! ${json}")
//        val dbobj2 = Gson().fromJson(json, DBObject::class.java)
//        Log.e(TAG, "JSON!! ${dbobj}")
//        Log.e(TAG, "JSON!! ${dbobj2}")



        try {
            Log.i("MyAmplifyApp", "Initialized Amplify")


            val config = AmplifyConfiguration.builder(applicationContext)
                .devMenuEnabled(false)
                .build()
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(config, App.getApp().applicationContext)
//

            Amplify.Auth.fetchAuthSession(
                { result ->
                    Log.i("AuthQuickStart1", result.toString())
                    setSubscription()
                },
                { error -> Log.e("AuthQuickStart1", error.toString()) }
            )

            Amplify.Auth.signInWithWebUI(
                this,
                { result ->
                    Log.i("AuthQuickStart2", result.toString())
                    setSubscription()
                },
                { error -> Log.e("AuthQuickStart2", error.toString()) }
            )

        } catch (error: AmplifyException) {
            Log.e("AuthQuickStart", "Could not initialize Amplify ${error.toString()}" )
        }
    }
    private fun setSubscription(){
        val nav = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        // https://issuetracker.google.com/issues/119800853
        // https://stackoverflow.com/questions/58703451/fragmentcontainerview-as-navhostfragment
        val arFragment = nav.childFragmentManager.primaryNavigationFragment
        (arFragment as ARFragment).setSubscription()
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