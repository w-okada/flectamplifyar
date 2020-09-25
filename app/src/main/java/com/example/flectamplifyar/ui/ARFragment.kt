package com.example.flectamplifyar.ui

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ThumbnailUtils
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.ApiOperation
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.graphql.model.ModelSubscription
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Canvas
import com.amplifyframework.datastore.generated.model.Element
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import com.example.flectamplifyar.dbmodel.DBObject
import com.example.flectamplifyar.helper.CameraConfigHelper
import com.example.flectamplifyar.helper.CameraPermissionHelper
import com.example.flectamplifyar.helper.DisplayRotationHelper
import com.example.flectamplifyar.helper.SnackbarHelper.showError
import com.example.flectamplifyar.helper.TapHelper
import com.example.flectamplifyar.model.StrokeProvider
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.arfragment.view.*
import kotlinx.android.synthetic.main.arfragment_menu.*
import kotlinx.android.synthetic.main.arfragment_menu.view.*
import kotlinx.android.synthetic.main.edit_text_view.*
import kotlinx.android.synthetic.main.image_capture_view.*
import kotlinx.android.synthetic.main.image_capture_view.view.*
import kotlinx.android.synthetic.main.load_marker_view.view.*
import kotlinx.android.synthetic.main.select_image_view.*
import java.io.File
import java.util.*
import javax.vecmath.Vector3f

class ARFragment(): Fragment(){
    companion object {
        private val TAG: String = ARFragment::class.java.getSimpleName()
        val MARKER_WIDTH = 500
        val MARKER_HEIGHT = 500
    }

    enum class Mode{
        LINE,
        TEXT,
        IMAGE,
    }

    var mode = Mode.LINE

    interface AROperationListener{
        fun uploadMarker(bitmap:Bitmap, filename:String, displayName:String, onSuccess:((uploadId:String, score:Int) -> Unit), onError:((message:String) -> Unit))
        fun setCurrentMarker(id:String, onSuccess:((message:String) -> Unit), onError:((message:String) -> Unit))
        fun getMarkers(onSuccess:((markers:List<Marker>) -> Unit), onError:((message:String) -> Unit))
        fun updateDB(uuid:String, json:String, add:Boolean,  onSuccess:((message:String) -> Unit), onError:((message:String) -> Unit))
        fun clearMyElements(onSuccess:((message:String) -> Unit), onError:((message:String) -> Unit))
    }

    var arOperationListener:AROperationListener? = null
    private var currentMarkerBitmap:Bitmap? = null

    var textElementText:String=""
    var imageElementBitmap:Bitmap? = null

    var session: Session? = null


    private var installRequested = false
    lateinit private var imageDatabase: AugmentedImageDatabase

    // Lifecyle
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.fragment,0,0)
        val param = a.getString(R.styleable.fragment_param);
        a.recycle()
        Log.e(TAG, "Inflateing... ")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        Log.e(TAG, "onCreateView... ")
        return inflater.inflate(R.layout.arfragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated... ")

        DisplayRotationHelper.setup(requireActivity())
        TapHelper.setup(requireActivity())
        StrokeProvider.setup(this)

        // recordableSurfaceViewのSetup
        rSurfaceView.rendererCallbacks = ARFragmentSurfaceRenderer
        ARFragmentSurfaceRenderer.setup(this)
        rSurfaceView.setOnTouchListener(TapHelper)

        // menuController Setup
        menuController.arFragment = this
        // ImageCaptureViewSetup
        imageCaptureView2.arFragment=this
        // LoadMarkerViewSetup
        loadMarkerView2.arFragment = this

        edit_text_view2.arFragment = this
        select_image_view2.arFragment = this
        select_image_view2.generateGird()


        main_marker_view.setOnClickListener {
            if(currentMarkerBitmap != null){
                refreshImageDatabase(currentMarkerBitmap!!, true)
            }
        }

        main_trash_button.setOnClickListener{
            if(currentMarkerBitmap != null){
                StrokeProvider.initialize()
                arOperationListener!!.clearMyElements(
                    {},{}
                )
            }
        }

        main_line_button.setOnClickListener {
            mode = Mode.LINE
            main_line_button.setBackgroundColor(Color.parseColor("#ffff0000"))
            main_image_button.setBackgroundColor(Color.parseColor("#00000000"))
            main_text_button.setBackgroundColor(Color.parseColor("#00000000"))
        }
        main_image_button.setOnClickListener {
            select_image_view.visibility = View.VISIBLE
            mode = Mode.IMAGE
            main_line_button.setBackgroundColor(Color.parseColor("#00000000"))
            main_image_button.setBackgroundColor(Color.parseColor("#ffff0000"))
            main_text_button.setBackgroundColor(Color.parseColor("#00000000"))
        }

        main_text_button.setOnClickListener {
            edit_text_view.visibility = View.VISIBLE
            mode = Mode.TEXT
            main_line_button.setBackgroundColor(Color.parseColor("#00000000"))
            main_image_button.setBackgroundColor(Color.parseColor("#00000000"))
            main_text_button.setBackgroundColor(Color.parseColor("#ffff0000"))
        }



    }


    override fun onResume() {
        Log.e(TAG,"onResume....")
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null


            // ARCore 設定
            try {
                when (ArCoreApk.getInstance().requestInstall(requireActivity(), !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> { }
                    else-> { }
                }

                // Camera Permission
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    CameraPermissionHelper.requestCameraPermission(requireActivity())
                    return
                }

                // Create the session.
                session = Session(context)
//                session = Session(this, EnumSet.of(Session.Feature.FRONT_CAMERA)) // Faceをするときに使う。。

                val config = session!!.getConfig()
                config.depthMode = if(session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
                    Config.DepthMode.AUTOMATIC
                } else {
                    Config.DepthMode.DISABLED
                }

                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

//                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                imageDatabase = AugmentedImageDatabase(session)
                config.augmentedImageDatabase = imageDatabase
                config.focusMode = Config.FocusMode.AUTO
                session!!.configure(config)

                CameraConfigHelper.setup(session)
                session!!.cameraConfig = CameraConfigHelper.getCurrentCameraConfig()


            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }

            if (message != null) {
                showError(requireActivity(), message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }


        // ARCore 開始
        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            showError(requireActivity(), "Camera not available. Try restarting the app.")
            session = null
            return
        }
        rSurfaceView.resume()
        DisplayRotationHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (session != null) {
            DisplayRotationHelper.onPause()
            rSurfaceView.pause()
            //glSurfaceView.onPause()
            session!!.pause()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            Toast.makeText(context, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(requireActivity())) {
                CameraPermissionHelper.launchPermissionSettings(requireActivity())
            }
            requireActivity().finish()
            requireActivity().moveTaskToBack(true);
        }
    }

    fun refreshImageDatabase(bitmap:Bitmap, keepPreviousElement:Boolean = false){
        Log.e(TAG, "setBM ${bitmap}")
        currentMarkerBitmap = bitmap
        val config = session!!.getConfig()
        imageDatabase = AugmentedImageDatabase(session)
        imageDatabase.addImage("mmm_${session!!.config.augmentedImageDatabase.numImages}",bitmap, 0.1f)
        config.augmentedImageDatabase = imageDatabase
        session!!.configure(config)
        ARFragmentSurfaceRenderer.anchor = null
        if(keepPreviousElement == true){

        }else{
            StrokeProvider.initialize()
        }
    }




//    var currentMarker:Marker? = null
//    fun setCurrentMarker(id:String, bitmap:Bitmap){
//        Log.e("APP","marker id ${id}")
////        App.getApp().selectedMarkerId = id           //TODO id, bitmapはLoadMarker時にFragment間でのデータの受け渡しになるが、ここがうまく作れていない。
////        App.getApp().selectedMarkerBitmap = bitmap
//
//        Amplify.API.query(
//            ModelQuery.get(Marker::class.java, id),
//            { response ->
//                Log.e("---------------------","RESPONSE!: ${response.data}")
//                if(response.data == null){
//                    Log.e("MyAmplifyApp", "Query failure NULL!")
//                    return@query
//                }
//                val marker = response.data
//                currentMarker = marker
//                if(marker.canvases.size==0){
//                    val canvas = Canvas.Builder()
//                        .title("")
//                        .owner("")
//                        .id(UUID.randomUUID().toString())
//                        .marker(marker)
//                        .build()
//
//                    Amplify.API.mutate(
//                        ModelMutation.create(canvas),
//                        { response -> Log.i("MyAmplifyApp", "Create Canvas with id: " + response) },
//                        { error -> Log.e("MyAmplifyApp", "Create Canvas failed", error) }
//                    )
//                }
//            },
//            { error -> Log.e("MyAmplifyApp", "Query failure", error) }
//        )
//        refreshImageDatabase(bitmap)
//
//    }



}
