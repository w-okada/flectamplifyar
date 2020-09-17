package com.example.flectamplifyar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.helper.FullScreenHelper


class MainActivity : AppCompatActivity(){
    companion object {
        private val TAG: String = MainActivity::class.java.getSimpleName()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            Log.i("MyAmplifyApp", "Initialized Amplify")

            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(App.getApp().applicationContext)
//

            Amplify.Auth.fetchAuthSession(
                { result -> Log.i("AuthQuickStart1", result.toString()) },
                { error -> Log.e("AuthQuickStart1", error.toString()) }
            )

            Amplify.Auth.signInWithWebUI(
                this,
                { result -> Log.i("AuthQuickStart2", result.toString()) },
                { error -> Log.e("AuthQuickStart2", error.toString()) }
            )

        } catch (error: AmplifyException) {
            Log.e("AuthQuickStart", "Could not initialize Amplify ${error.toString()}" )
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