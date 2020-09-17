package com.example.flectamplifyar

import android.content.Intent
import android.graphics.*
import android.graphics.ImageFormat
import android.media.Image
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.helper.*
import com.example.flectamplifyar.helper.SnackbarHelper.showError
import com.example.flectamplifyar.model.Stroke
import com.example.flectamplifyar.model.StrokeProvider
import com.example.flectamplifyar.rendering.*
import com.example.flectamplifyar.ui.MarkerSpinnerAdapter
import com.google.ar.core.*
import com.google.ar.core.Camera
import com.google.ar.core.exceptions.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.vecmath.Vector2f
import javax.vecmath.Vector3f


//class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {
//

//



//

//
//    private var mAnchor: Anchor? = null
//    private val SEARCHING_PLANE_MESSAGE = "Searching for surfaces..."
//
//    private var captureNextFrame = false
//

//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        // Amplify Initialization
//        try {
//            Log.i("MyAmplifyApp", "Initialized Amplify")
//
//            Amplify.addPlugin(AWSCognitoAuthPlugin())
//            Amplify.addPlugin(AWSS3StoragePlugin())
//            Amplify.addPlugin(AWSApiPlugin())
//            Amplify.configure(applicationContext)
////
//
//            Amplify.Auth.fetchAuthSession(
//                { result -> Log.i("AmplifyQuickstart", result.toString()) },
//                { error -> Log.e("AmplifyQuickstart", error.toString()) }
//            )
////            Amplify.Auth.signOut(
////                { Log.i("AuthQuickstart", "Signed out successfully") },
////                { error -> Log.e("AuthQuickstart", error.toString()) }
////            )
//
//            Amplify.Auth.signInWithWebUI(
//                this,
//                { result -> Log.i("AuthQuickStart", result.toString()) },
//                { error -> Log.e("AuthQuickStart", error.toString()) }
//            )
//
//        } catch (error: AmplifyException) {
//            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
//        }
//
//
//
//        // UI Initialization
//        setContentView(R.layout.activity_main)
//
//        DisplayRotationHelper.setup(this)
//        TapHelper.setup(this)
//        TrackingStateHelper.setup(this)
//
//        surfaceview.setOnTouchListener(TapHelper)
//
//        surfaceview.preserveEGLContextOnPause = true
//        surfaceview.setEGLContextClientVersion(2)
//        surfaceview.setEGLConfigChooser(8, 8, 8, 8, 16, 0)// Alpha used for plane blending.
//        surfaceview.setRenderer(this)
//        surfaceview.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//        surfaceview.setWillNotDraw(false)
//
//
//        val spinner = findViewById<Spinner>(R.id.menu)
//
//        captureMarker.setOnClickListener {
//            captureNextFrame = true
//            menu.visibility = when(menu.visibility){
//                View.GONE -> View.VISIBLE
//                View.VISIBLE -> View.GONE
//                else -> View.VISIBLE
//            }
//        }
//
//
//        val spinnerItems = arrayOf(
//            "Select", "Yuka", "Kurumi", "Mai", "Miki", "Saya",
//            "Toko", "Nagi", "Yuyu", "Yumiko", "Katakuriko"
//        )
//
//        val spinnerImages = arrayOf(
//            "transparent", "yuka", "kurumi", "mai", "miki", "saya",
//            "toko", "nagi", "yuyu", "yumiko", "katakuriko"
//        )
//
//
//        val adapter = MarkerSpinnerAdapter(this.applicationContext, spinnerItems, spinnerImages)
//
//        spinner.adapter = adapter
//
//
//        // リスナーを登録
//        spinner.onItemSelectedListener = object : OnItemSelectedListener {
//            //　アイテムが選択された時
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                viw: View, position: Int, id: Long
//            ) {
//                Log.e("----------------------","aaaaaaaaaaa")
//            }
//
//            //　アイテムが選択されなかった
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
//
////
////        var items = arrayOf("Drawing", "Load", "")
////        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        menu.adapter=adapter
////
////        menu.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
////            //　アイテムが選択された時
////            override fun onItemSelected(
////                parent: AdapterView<*>,
////                view: View?, position: Int, id: Long
////            ) {
////                val menu: Spinner = parent as Spinner
////                val item = menu.getSelectedItem() as String
////            }
////
////            //　アイテムが選択されなかった
////            override fun onNothingSelected(parent: AdapterView<*>?) {
////                //
////            }
////        })
//        calculateUVTransform = true
//        // Prerequisit
//        installRequested = false
//    }
//
//
//
//    private fun uploadFile() {
//        val exampleFile = File(applicationContext.filesDir, "ExampleKey")
//
//        exampleFile.writeText("Example file contents")
//
//        Amplify.Storage.uploadFile(
//            "ExampleKey",
//            exampleFile,
//            { result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()) },
//            { error -> Log.e("MyAmplifyApp", "Upload failed", error) }
//        )
//    }
//
//

//

//
//    private fun convertWorldCordPoint(touchPoint: Vector2f, frame: Frame, camera: Camera): Vector3f {
//        val projmtx = FloatArray(16)
//        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
//        val viewmtx = FloatArray(16)
//        camera.getViewMatrix(viewmtx, 0)
//        return  LineUtils.getWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx)
//    }
//
//    private val mSharedStrokes: MutableMap<String, Stroke> = HashMap() // pair-partner's stroke
//    private fun handleTap(frame: Frame, camera: Camera) {
//        val tap = TapHelper.poll()
////        Log.e("-----TAP:::", "tapinfo: ${tap.toString()}")
//
//        if(tap !== null){
//            val mLastTouch = Vector2f(tap.x, tap.y)
//            //addPoint2f(mLastTouch, frame, camera)
//            var point = convertWorldCordPoint(mLastTouch, frame, camera)
//            if (mAnchor != null && mAnchor!!.trackingState == TrackingState.TRACKING) {
//                point = LineUtils.transformPointToPose(point, mAnchor!!.getPose())
//                StrokeProvider.addNewEvent(tap, point)
//            }
//        }
//
//        if(StrokeProvider.mStrokes.size >0) {
//            LineShaderRenderer.setColor(AppSettings.color)
//            LineShaderRenderer.mDrawDistance = AppSettings.strokeDrawDistance
//            val distanceScale = 1.0f
//            LineShaderRenderer.setDistanceScale(distanceScale)
//            LineShaderRenderer.setLineWidth(0.33f)
//            LineShaderRenderer.clear()
//            LineShaderRenderer.updateStrokes(StrokeProvider.mStrokes, mSharedStrokes) // pair-partner's stroke
//            LineShaderRenderer.upload()
//        }
//    }
//
//
//
//    /** Checks if we detected at least one plane.  */
//    private fun hasTrackingPlane(): Boolean {
//        for (plane in session!!.getAllTrackables(Plane::class.java)) {
//            if (plane.trackingState == TrackingState.TRACKING) {
//                return true
//            }
//        }
//        return false
//    }
//}