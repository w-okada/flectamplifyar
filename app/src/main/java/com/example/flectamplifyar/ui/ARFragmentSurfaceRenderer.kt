package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import android.view.View
import com.example.flectamplifyar.R
import com.example.flectamplifyar.helper.DepthSettings
import com.example.flectamplifyar.helper.DisplayRotationHelper
import com.example.flectamplifyar.helper.TapHelper
import com.example.flectamplifyar.helper.TrackingStateHelper
import com.example.flectamplifyar.model.Stroke
import com.example.flectamplifyar.model.StrokeProvider
import com.example.flectamplifyar.rendering.*
import com.google.ar.core.*
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.image_capture_view.*
import java.io.IOException
import javax.vecmath.Vector2f
import javax.vecmath.Vector3f


object ARFragmentSurfaceRenderer: RecordableSurfaceView.RendererCallbacks {
    private val TAG = ARFragmentSurfaceRenderer::class.java.simpleName
    private var calculateUVTransform = true
    var anchor: Anchor? = null

    private var mScreenWidth  = 0.0f
    private var mScreenHeight = 0.0f

//    var captureNextFrame = false

    private lateinit var context: Context
    private lateinit var arFragment:ARFragment
    fun setup(fragment: ARFragment){
        Log.e(TAG, "setup ARFRagment!!!")
        arFragment=fragment
        context = arFragment.requireContext()
    }
    override fun onSurfaceCreated() {

    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        var rotation = Surface.ROTATION_0

//        if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) {
//            rotation = Surface.ROTATION_90
//        }
        arFragment.session!!.setDisplayGeometry(rotation, width, height)
        mScreenWidth = width.toFloat()
        mScreenHeight = height.toFloat()
        GLES20.glViewport(0, 0, width, height)
    }




    override fun onSurfaceDestroyed() {
//        mBackgroundRenderer.clearGL()
//        mLineShaderRenderer.clearGL()
    }

    override fun onContextCreated() {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            DepthTexture .createOnGlThread()
            BackgroundRenderer.createOnGlThread(context, DepthTexture.getTextureId())

            // GLES
            GLES.makeProgram(context)
            GLES.setDepthTexture(DepthTexture.getTextureId(), DepthTexture.getWidth(), DepthTexture.getHeight())
            TextureRect.setup()
//            StringTexture.makeStringTexture("FLECT", 20f, Color.parseColor("#FFFFFFFF"), Color.parseColor("#55FF0000"))
//            StringTexture.makeColorTexture(Color.parseColor("#55FF0000"))
            TextureRect.makeImageTexture(context,  R.drawable.earthpicture)

            TextureTriangl.setup()
            TextureTriangl.makeImageTexture(context,  R.drawable.earthpicture)


//            TextureSphere.makeImageTexture(context,  R.drawable.earthpicture)
//            TextureSphere.makeStringTexture("FLECT", 20f, Color.parseColor("#FFFFFFFF"), Color.parseColor("#55FF0000"))
            TextureSphere.makeColorTexture(Color.parseColor("#55FF0000"))

            TextureLine.makeColorTexture(Color.parseColor("#55FF0000"))


            TextureCube.setup()
            TextureCube.makeColorTexture(Color.parseColor("#55FF0000"))
            TextureCube.makeImageTexture(context,  R.drawable.earthpicture)


            //デプスバッファの有効化
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            // カリングの有効化
//            GLES20.glEnable(GLES20.GL_CULL_FACE) //裏面を表示しないチェックを行う
            GLES20.glFrontFace(GLES20.GL_CCW) //表面のvertexのindex番号はCCWで登録
            GLES20.glCullFace(GLES20.GL_BACK) //裏面は表示しない
            // 背景とのブレンド方法を設定します。
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) // 単純なアルファブレンド


            GLES20.glUniform4f(GLES.lightAmbientHandle, 0.15f, 0.15f, 0.15f, 1.0f) //周辺光
            GLES20.glUniform4f(GLES.lightDiffuseHandle, 0.5f, 0.5f, 0.5f, 1.0f) //乱反射光
            GLES20.glUniform4f(GLES.lightSpecularHandle, 0.9f, 0.9f, 0.9f, 1.0f) //鏡面反射光


            calculateUVTransform = true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    override fun onPreDrawFrame() {
//        update()
    }

    override fun onDrawFrame() {
        val renderStartTime = System.currentTimeMillis()
        renderScene()
//        mRenderDuration = System.currentTimeMillis() - renderStartTime
    }

    private fun renderScene(){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val session = arFragment.session
        if (session == null) {
            return
        }


        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        DisplayRotationHelper.updateSessionIfNeeded(session)
        try {
            session.setCameraTextureName(BackgroundRenderer.getTextureId())

            val frame = session.update()
            val camera = frame.camera
            val rotate = DisplayRotationHelper.getCameraSensorToDisplayRotation(session.cameraConfig.cameraId)

            BackgroundRenderer.draw(frame, DepthSettings.depthColorVisualizationEnabled())

            if (arFragment.imageCaptureView.visibility == View.VISIBLE){
                return
            }


            // アンカー作成
            val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
            for(i in updatedAugmentedImages){
//                Log.e(TAG,"AUGMENTED IMAGE ${i.name}")
                if(i.trackingState != TrackingState.TRACKING){
//                    Log.e(TAG, "not tracking!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    continue
                }
                if(i.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING){
//                    Log.e(TAG, "not !!FULL!! tracking!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    continue
                }
                if(anchor == null){
                    anchor = session.createAnchor(i.centerPose)
                }
            }

            if(anchor == null){
//                Log.e(TAG,"no Anchor!!!!!")
                return
            }else{
//                Log.e(TAG,"Anchor!!!!! ${anchor!!.pose.tx()}, ${anchor!!.pose.ty()}, ${anchor!!.pose.tz()}")
            }


            // UV Transform
            if (frame.hasDisplayGeometryChanged() || calculateUVTransform) {
                calculateUVTransform = false
                val transform: FloatArray = getTextureTransformMatrix(frame)
                GLES.setUvTransformMatrix(transform)
            }


            // Depthを取得
            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                DepthTexture.updateWithDepthImageOnGlThread(frame)
            }

            handleTap(frame, camera)
//
            TrackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
//            // If not tracking, don't draw 3D objects, show tracking failure reason instead.
//            if (camera.trackingState == TrackingState.PAUSED) {
//                SnackbarHelper.showMessage(
//                    this, TrackingStateHelper.getTrackingFailureReasonString(camera)
//                )
//                return
//            }
//

            // Get projection&view matrix.
            val projmtx = FloatArray(16)
            val viewmtx = FloatArray(16)
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
            camera.getViewMatrix(viewmtx, 0)
//
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)
//
//            if (hasTrackingPlane()) {
//                SnackbarHelper.hide(this)
//            } else {
//                SnackbarHelper.showMessage(this, SEARCHING_PLANE_MESSAGE)
//            }
//



            if(true){

                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                GLES.useProgram()
                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, dummyBuffer)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture0")

                //無変換の記述はここ
//                GLES.enableShading()
//                GLES.disableTexture()
                GLES.disableShading()
                GLES.enableTexture()

                GLES.setPMatrix(projmtx)
                GLES.setCMatrix(viewmtx)

                Matrix.setIdentityM(mMatrix, 0)
                val aMatrix = FloatArray(16) //モデル変換マトリックス
                Matrix.setIdentityM(aMatrix, 0)
                anchor!!.pose.toMatrix(aMatrix, 0)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture1")

                GLES.updateMatrix(mMatrix, aMatrix)

                TextureLine.makeColorTexture(Color.parseColor("#FFFFFFFF"))

                for (l in StrokeProvider.mStrokes){
                    TextureLine.makeAStroke(l)
                    TextureLine.draw(1f, 0f, 0f, 1f, 10.0f, 20f);//座標軸の描画本体
                }
            }

            if(true){

                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                GLES.useProgram()
                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, dummyBuffer)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture0")

                //無変換の記述はここ
//                GLES.enableShading()
//                GLES.disableTexture()
                GLES.disableShading()
                GLES.enableTexture()

                GLES.setPMatrix(projmtx)
                GLES.setCMatrix(viewmtx)

                Matrix.setIdentityM(mMatrix, 0)
                val aMatrix = FloatArray(16) //モデル変換マトリックス
                Matrix.setIdentityM(aMatrix, 0)
                anchor!!.pose.toMatrix(aMatrix, 0)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture1")

                GLES.updateMatrix(mMatrix, aMatrix)

                TextureLine.makeColorTexture(Color.parseColor("#ffffff00"))

                for (l in StrokeProvider.mOthersStrokes){
                    TextureLine.makeAStroke(l)
                    TextureLine.draw(1f, 0f, 0f, 1f, 10.0f, 20f);//座標軸の描画本体
                }
            }
//
//
            for(l in StrokeProvider.mStrokes){

                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                GLES.useProgram()
                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, dummyBuffer)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture0")

                //無変換の記述はここ
                GLES.disableShading()
                GLES.enableTexture()
                GLES.setPMatrix(projmtx)

                GLES.setCMatrix(viewmtx)

                Matrix.setIdentityM(mMatrix, 0)

                val aMatrix = FloatArray(16) //モデル変換マトリックス
//                Matrix.setIdentityM(aMatrix, 0)
                anchor!!.pose.toMatrix(aMatrix, 0)

                Matrix.translateM(mMatrix, 0, l.getPoints()[0].x, l.getPoints()[0].y, l.getPoints()[0].z)
//                Matrix.translateM(mMatrix, 0, 0.1f, 0f, 0f)
                Matrix.scaleM(mMatrix, 0, 0.1f, 0.1f, -0.1f)
                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture1")

                GLES.updateMatrix(mMatrix, aMatrix)
                TextureRect.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)
            }

            for(l in StrokeProvider.mStrokes){

                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                GLES.useProgram()
                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, dummyBuffer)

                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture0")

                //無変換の記述はここ
//                GLES.enableShading()
//                GLES.disableTexture()
                GLES.disableShading()
                GLES.enableTexture()
                GLES.setPMatrix(projmtx)

                GLES.setCMatrix(viewmtx)

                //大きい地球の最前面にHelloを表示
                Matrix.setIdentityM(mMatrix, 0)

                val aMatrix = FloatArray(16) //モデル変換マトリックス
                Matrix.setIdentityM(aMatrix, 0)
//                anchor!!.pose.toMatrix(aMatrix, 0)

//                Matrix.translateM(mMatrix, 0, l.getPoints()[0].x, l.getPoints()[0].y, l.getPoints()[0].z)
                Matrix.translateM(mMatrix, 0, 0.1f, 0.0f, 0.1f)
                Matrix.scaleM(mMatrix, 0, 0.05f, 0.05f, 0.05f);
                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture1")

                GLES.updateMatrix(mMatrix, aMatrix)
                TextureSphere.draw(1f, 1f, 1f, 1f, 5.0f);
            }


            for(i in updatedAugmentedImages){
//                if(i.trackingState != TrackingState.TRACKING){
//                    Log.e(TAG, "not tracking!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
//                    continue
//                }
//                if(i.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING){
//                    Log.e(TAG, "not !!FULL!! tracking!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
//                    continue
//                }
                GLES.useProgram()

                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, dummyBuffer)
                GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, dummyBuffer)



                GLES.disableShading()
                GLES.enableTexture()
                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                val aMatrix = FloatArray(16) //モデル変換マトリックス
                Matrix.setIdentityM(aMatrix, 0)
//                Matrix.setIdentityM(mMatrix, 0)
//                i.centerPose.toMatrix(aMatrix,0)
                anchor!!.pose.toMatrix(aMatrix, 0)
//                Log.e(TAG,"center!!!!! ${aMatrix}")
                GLES.setPMatrix(projmtx)
                GLES.setCMatrix(viewmtx)

                Matrix.setIdentityM(mMatrix, 0)
//                Matrix.translateM(mMatrix, 0, 0.5f, 0.1f, -0.3f)
                Matrix.scaleM(mMatrix, 0, 0.005f, 0.005f, 0.005f);
                GLES.updateMatrix(mMatrix, aMatrix)
//                TextureTriangl.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)
//                TextureCube.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)
                TextureCube.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)



            }





        } catch (t: Throwable) {
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }
    }

    private fun getTextureTransformMatrix(frame: Frame): FloatArray {
        val frameTransform = FloatArray(6)
        val uvTransform = FloatArray(9)
        // XY pairs of coordinates in NDC space that constitute the origin and points along the two
        // principal axes.
        val ndcBasis = floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f)

        // Temporarily store the transformed points into outputTransform.
        frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            ndcBasis,
            Coordinates2d.TEXTURE_NORMALIZED,
            frameTransform
        )

        // Convert the transformed points into an affine transform and transpose it.
        val ndcOriginX = frameTransform[0]
        val ndcOriginY = frameTransform[1]
        uvTransform[0] = frameTransform[2] - ndcOriginX
        uvTransform[1] = frameTransform[3] - ndcOriginY
        uvTransform[2] = 0.0f
        uvTransform[3] = frameTransform[4] - ndcOriginX
        uvTransform[4] = frameTransform[5] - ndcOriginY
        uvTransform[5] = 0.0f
        uvTransform[6] = ndcOriginX
        uvTransform[7] = ndcOriginY
        uvTransform[8] = 1.0f
        return uvTransform
    }

    private fun handleTap(frame: Frame, camera: Camera) {

        while(true){
            val tap = TapHelper.poll()
            if(tap !== null){
                val mLastTouch = Vector2f(tap.x, tap.y)
                //addPoint2f(mLastTouch, frame, camera)
                var point = convertWorldCordPoint(mLastTouch, frame, camera)
                if (anchor != null) {
                    point = LineUtils.transformPointToPose(point, anchor!!.getPose())
                    StrokeProvider.addNewEvent(tap, point)
                }
            }else{
                break
            }

        }

    }


    private fun convertWorldCordPoint(touchPoint: Vector2f, frame: Frame, camera: Camera): Vector3f {
        val projmtx = FloatArray(16)
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
        val viewmtx = FloatArray(16)
        camera.getViewMatrix(viewmtx, 0)
        return  LineUtils.getWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx)
    }

}