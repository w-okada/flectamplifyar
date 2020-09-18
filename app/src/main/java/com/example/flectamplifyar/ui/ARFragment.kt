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
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import com.example.flectamplifyar.helper.CameraConfigHelper
import com.example.flectamplifyar.helper.CameraPermissionHelper
import com.example.flectamplifyar.helper.DisplayRotationHelper
import com.example.flectamplifyar.helper.SnackbarHelper.showError
import com.example.flectamplifyar.helper.TapHelper
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.arfragment.view.*
import kotlinx.android.synthetic.main.arfragment_menu.*
import kotlinx.android.synthetic.main.arfragment_menu.view.*
import kotlinx.android.synthetic.main.image_capture_view.*
import kotlinx.android.synthetic.main.image_capture_view.view.*
import java.io.File
import java.util.*

class ARFragment(): Fragment(){
    companion object {
        private val TAG: String = ARFragment::class.java.getSimpleName()
        val MARKER_WIDTH = 500
        val MARKER_HEIGHT = 500

    }

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
        Log.e(TAG, "    Param: ${param}")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        return inflater.inflate(R.layout.arfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DisplayRotationHelper.setup(requireActivity())
        TapHelper.setup(requireActivity())

        rSurfaceView.rendererCallbacks = ARFragmentSurfaceRenderer
        ARFragmentSurfaceRenderer.setup(this)
    }




//        // UI Initialization
//        setContentView(R.layout.activity_main)
//
//        TrackingStateHelper.setup(this)

//        glSurfaceView.setOnTouchListener(TapHelper)

//        s.preserveEGLContextOnPause = true
//        s.setEGLContextClientVersion(2)
//        s.setEGLConfigChooser(8, 8, 8, 8, 16, 0)// Alpha used for plane blending.
//        ARFragmentRenderer.setup(this)
//        s.setRenderer(ARFragmentRenderer)
//        s.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//        s.setWillNotDraw(false)



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
//
//        // Prerequisit
//        installRequested = false
//
//







    override fun onResume() {
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null

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

        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            showError(requireActivity(), "Camera not available. Try restarting the app.")
            session = null
            return
        }
        rSurfaceView.resume()
        //glSurfaceView.onResume()
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



    fun setMarker(bm:Bitmap){
        Log.e(TAG, "setBM!!!!!!!!!!!!!!!")
        val config = session!!.getConfig()
        imageDatabase = AugmentedImageDatabase(session)
        imageDatabase.addImage("mmm_${session!!.config.augmentedImageDatabase.numImages}",bm, 0.1f)
        config.augmentedImageDatabase = imageDatabase
        session!!.configure(config)
        ARFragmentSurfaceRenderer.anchor = null
    }








}
