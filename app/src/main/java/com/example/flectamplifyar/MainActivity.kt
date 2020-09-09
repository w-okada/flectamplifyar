package com.example.flectamplifyar

import android.content.Intent
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.SnapHelper
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.helper.*
import com.example.flectamplifyar.helper.SnackbarHelper.showError
import com.example.flectamplifyar.rendering.BackgroundRenderer
import com.example.flectamplifyar.rendering.DepthTexture
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private var installRequested = false

    private var session: Session? = null
    private var calculateUVTransform = true
    lateinit private var imageDatabase: AugmentedImageDatabase

    private var mScreenWidth  = 0.0f
    private var mScreenHeight = 0.0f

    companion object {
        private val TAG: String = MainActivity::class.java.getSimpleName()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Amplify Initialization
        try {
            Log.i("MyAmplifyApp", "Initialized Amplify")

//            Amplify.addPlugin(AWSCognitoAuthPlugin())
//            Amplify.addPlugin(AWSS3StoragePlugin())
//            Amplify.addPlugin(AWSApiPlugin())
//            Amplify.configure(applicationContext)
//
//
//            Amplify.Auth.fetchAuthSession(
//                { result -> Log.i("AmplifyQuickstart", result.toString()) },
//                { error -> Log.e("AmplifyQuickstart", error.toString()) }
//            )
//
//            Amplify.Auth.signInWithWebUI(
//                this,
//                { result -> Log.i("AuthQuickStart", result.toString()) },
//                { error -> Log.e("AuthQuickStart", error.toString()) }
//            )
//
//            uploadFile()
//
//            val options: RestOptions = RestOptions.builder()
//                .addPath("/markers/abc")
//                .addBody("{\"name\":\"Mow the lawn\"}".toByteArray())
//                .build()
//
//            Amplify.API.post(options,
//                { response -> Log.i("MyAmplifyApp", "POST " + response.data.asString()) },
//                { error -> Log.e("MyAmplifyApp", "POST failed", error) }
//            )

        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }



        // UI Initialization
        setContentView(R.layout.activity_main)

        DisplayRotationHelper.setup(this)
        TapHelper.setup(this)
        TrackingStateHelper.setup(this)

        surfaceview.setOnTouchListener(TapHelper)

        surfaceview.preserveEGLContextOnPause = true
        surfaceview.setEGLContextClientVersion(2)
        surfaceview.setEGLConfigChooser(8, 8, 8, 8, 16, 0)// Alpha used for plane blending.
        surfaceview.setRenderer(this)
        surfaceview.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceview.setWillNotDraw(false)

        calculateUVTransform = true

        // Prerequisit
        installRequested = false
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null

            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> { }
                    else-> { }
                }

                // Camera Permission
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                // Create the session.
                session = Session(this)
                val config = session!!.getConfig()
                config.depthMode = if(session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
                    Config.DepthMode.AUTOMATIC
                } else {
                    Config.DepthMode.DISABLED
                }

                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

//                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                imageDatabase = AugmentedImageDatabase(session);
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
                showError(this, message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }

        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            showError(this, "Camera not available. Try restarting the app.")
            session = null
            return
        }
        surfaceview.onResume()
        DisplayRotationHelper.onResume()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE) {
            Amplify.Auth.handleWebUISignInResponse(data)
        }
    }

    private fun uploadFile() {
        val exampleFile = File(applicationContext.filesDir, "ExampleKey")

        exampleFile.writeText("Example file contents")

        Amplify.Storage.uploadFile(
            "ExampleKey",
            exampleFile,
            { result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()) },
            { error -> Log.e("MyAmplifyApp", "Upload failed", error) }
        )
    }


    override fun onPause() {
        super.onPause()
        if (session != null) {
            DisplayRotationHelper.onPause()
            surfaceview.onPause()
            session!!.pause()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }



    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            DepthTexture .createOnGlThread()
            BackgroundRenderer.createOnGlThread(this, DepthTexture.getTextureId())
//            planeRenderer.createOnGlThread( /*context=*/this, "models/trigrid.png")
//            pointCloudRenderer.createOnGlThread( /*context=*/this)
//            Log.e("----","fin load all file0")
//            virtualObject.createOnGlThread( /*context=*/this, "models/andy.obj", "models/andy.png")
//            Log.e("----","fin load all file1")
//
//            virtualObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending)
//            Log.e("----","fin load all file2")
//            virtualObject.setDepthTexture(
//                depthTexture.getTextureId(), depthTexture.getWidth(), depthTexture.getHeight()
//            )

//            GLES.setDepthTexture(
//                depthTexture.getTextureId(), depthTexture.getWidth(), depthTexture.getHeight()
//            )
//            Log.e("----","fin load all file3")
//            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f)
//            Log.e("----","fin load all file4")

//            mLineShaderRenderer.createOnGlThread(this)
//            //プログラムの生成
//            validProgram = makeProgram(this)
//
//
//            //頂点配列の有効化
//            GLES20.glEnableVertexAttribArray(GLES.positionHandle)
//            GLES20.glEnableVertexAttribArray(GLES.normalHandle)
//            GLES20.glEnableVertexAttribArray(GLES.texcoordHandle)

            //デプスバッファの有効化
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)

//            // カリングの有効化
            //GLES20.glEnable(GLES20.GL_CULL_FACE) //裏面を表示しないチェックを行う

            // 裏面を描画しない
            GLES20.glFrontFace(GLES20.GL_CCW) //表面のvertexのindex番号はCCWで登録
            //GLES20.glCullFace(GLES20.GL_BACK) //裏面は表示しない


//            //光源色の指定 (r, g, b,a)
//            GLES20.glUniform4f(GLES.lightAmbientHandle, 0.15f, 0.15f, 0.15f, 1.0f) //周辺光
//            GLES20.glUniform4f(GLES.lightDiffuseHandle, 0.5f, 0.5f, 0.5f, 1.0f) //乱反射光
//            GLES20.glUniform4f(GLES.lightSpecularHandle, 0.9f, 0.9f, 0.9f, 1.0f) //鏡面反射光

            //背景色の設定
            GLES20.glClearColor(0f, 0f, 0.2f, 1.0f)

            //テクスチャの有効化
//            GLES20.glEnable(GLES20.GL_TEXTURE_2D)

            // 背景とのブレンド方法を設定します。
            GLES20.glEnable(GLES20.GL_BLEND)
//            ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "after alpha123 - 2")
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) // 単純なアルファブレンド

//            Hello = StringTexture(
//                "明後日の方向", 10f, Color.WHITE, Color.parseColor("#000F00C0")
//            )

        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        mScreenWidth  = width.toFloat()
        mScreenHeight = height.toFloat()
        DisplayRotationHelper.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (session == null) {
            return
        }

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        DisplayRotationHelper.updateSessionIfNeeded(session!!)
        try {
            session!!.setCameraTextureName(BackgroundRenderer.getTextureId())

            val frame = session!!.update()
            val camera = frame.camera
            val rotate = DisplayRotationHelper.getCameraSensorToDisplayRotation(session!!.cameraConfig.cameraId)

            if (frame.hasDisplayGeometryChanged() || calculateUVTransform) {
//                // The UV Transform represents the transformation between screenspace in normalized units
//                // and screenspace in units of pixels.  Having the size of each pixel is necessary in the
//                // virtual object shader, to perform kernel-based blur effects.
//                calculateUVTransform = false
//                val transform: FloatArray = getTextureTransformMatrix(frame)
//                Log.e("-------------------","TextureTransformMatrix ${transform.contentToString()}")
//                virtualObject.setUvTransformMatrix(transform)
//                GLES.setUvTransformMatrix(transform)

            }

            if (session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                DepthTexture.updateWithDepthImageOnGlThread(frame)
            }

            handleTap(frame, camera)
            BackgroundRenderer.draw(frame, DepthSettings.depthColorVisualizationEnabled())

            TrackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
            // If not tracking, don't draw 3D objects, show tracking failure reason instead.
            if (camera.trackingState == TrackingState.PAUSED) {
                SnackbarHelper.showMessage(
                    this, TrackingStateHelper.getTrackingFailureReasonString(camera)
                )
                return
            }

        } catch (t: Throwable) {
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }
    }


    private fun handleTap(frame: Frame, camera: Camera) {
        val tap = TapHelper.poll()
//        Log.e("-----TAP:::", "tapinfo: ${tap.toString()}")

//        if(tap !== null){
//            val mLastTouch = Vector2f(tap.x, tap.y)
//            //addPoint2f(mLastTouch, frame, camera)
//            var point = convertWorldCordPoint(mLastTouch, frame, camera)
//            if (mAnchor != null && mAnchor!!.trackingState == TrackingState.TRACKING) {
//                point = LineUtils.TransformPointToPose(point, mAnchor!!.getPose());
//                StrokeProvider.addNewEvent(tap, point)
//            }
//        }
//
//        mLineShaderRenderer.setColor(AppSettings.color)
//        mLineShaderRenderer.mDrawDistance = AppSettings.strokeDrawDistance
//        val distanceScale = 1.0f
//        mLineShaderRenderer.setDistanceScale(distanceScale)
//        mLineShaderRenderer.setLineWidth(0.33f)
//        mLineShaderRenderer.clear()
//        mLineShaderRenderer.updateStrokes(StrokeProvider.mStrokes, mSharedStrokes)
//        mLineShaderRenderer.upload()
//
    }
}