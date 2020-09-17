package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Color
import android.media.ThumbnailUtils
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.flectamplifyar.helper.DepthSettings
import com.example.flectamplifyar.helper.DisplayRotationHelper
import com.example.flectamplifyar.helper.Utils
import com.example.flectamplifyar.rendering.*
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import kotlinx.android.synthetic.main.arfragment.*
import java.io.IOException
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


object ARFragmentRenderer : GLSurfaceView.Renderer{


    private val TAG = ARFragmentRenderer::class.java.simpleName
    private var mScreenWidth  = 0.0f
    private var mScreenHeight = 0.0f
    private var calculateUVTransform = true

    private lateinit var context: Context
    private lateinit var arFragment:ARFragment
    fun setup(fragment:ARFragment){
        arFragment=fragment
        context = arFragment.requireContext()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            DepthTexture .createOnGlThread()
            BackgroundRenderer.createOnGlThread(context, DepthTexture.getTextureId())
            LineShaderRenderer.createOnGlThread(context)

            // GLES
            GLES.makeProgram(context)
            GLES.setDepthTexture(DepthTexture.getTextureId(), DepthTexture.getWidth(), DepthTexture.getHeight())
            StringTexture.setup("FLECT", 20f, Color.parseColor("#FFFFFFFF"), Color.parseColor("#55FF0000"))
            TrianglTexture.setup("aaaa", 20f, Color.parseColor("#FFFFFFFF"), Color.parseColor("#55FF0000"))

            //デプスバッファの有効化
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            // カリングの有効化
//            GLES20.glEnable(GLES20.GL_CULL_FACE) //裏面を表示しないチェックを行う
            GLES20.glFrontFace(GLES20.GL_CCW) //表面のvertexのindex番号はCCWで登録
            GLES20.glCullFace(GLES20.GL_BACK) //裏面は表示しない
            // 背景とのブレンド方法を設定します。
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) // 単純なアルファブレンド


            calculateUVTransform = true
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

//
//            // Define Anchor
//            val trackables = session!!.getAllTrackables(Plane::class.java)
//            for(t in trackables){
//                if(mAnchor === null){
//                    mAnchor = session!!.createAnchor(camera.pose)
//                    mAnchor!!.pose.toMatrix(LineShaderRenderer.mModelMatrix, 0)
//                }
//            }
//
//
            if (frame.hasDisplayGeometryChanged() || calculateUVTransform) {
                // The UV Transform represents the transformation between screenspace in normalized units
                // and screenspace in units of pixels.  Having the size of each pixel is necessary in the
                // virtual object shader, to perform kernel-based blur effects.
                calculateUVTransform = false
                val transform: FloatArray = getTextureTransformMatrix(frame)
                GLES.setUvTransformMatrix(transform)

            }
//
//            if (session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
//                DepthTexture.updateWithDepthImageOnGlThread(frame)
//            }
//
//            handleTap(frame, camera)
            BackgroundRenderer.draw(frame, DepthSettings.depthColorVisualizationEnabled())
//
//            TrackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
//            // If not tracking, don't draw 3D objects, show tracking failure reason instead.
//            if (camera.trackingState == TrackingState.PAUSED) {
//                SnackbarHelper.showMessage(
//                    this, TrackingStateHelper.getTrackingFailureReasonString(camera)
//                )
//                return
//            }
//

//            // Get projection&view matrix.
//            val projmtx = FloatArray(16)
//            val viewmtx = FloatArray(16)
//            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)
//            camera.getViewMatrix(viewmtx, 0)
//
//            val colorCorrectionRgba = FloatArray(4)
//            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)
//
//            if (hasTrackingPlane()) {
//                SnackbarHelper.hide(this)
//            } else {
//                SnackbarHelper.showMessage(this, SEARCHING_PLANE_MESSAGE)
//            }
//
//            if(StrokeProvider.mStrokes.size>0){
//                LineShaderRenderer.draw(
//                    viewmtx, projmtx, mScreenWidth, mScreenHeight,
//                    AppSettings.nearClip, AppSettings.farClip
//                )
//            }
//
//
//            for(l in StrokeProvider.mStrokes){
//
//                //変換マトリックス
//                val mMatrix = FloatArray(16) //モデル変換マトリックス
//                GLES.useProgram()
//                val dummyFloat = FloatArray(1)
//                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
//                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
//                GLES20.glVertexAttribPointer(
//                    GLES.positionHandle,
//                    3,
//                    GLES20.GL_FLOAT,
//                    false,
//                    0,
//                    dummyBuffer
//                )
//                GLES20.glVertexAttribPointer(
//                    GLES.normalHandle,
//                    3,
//                    GLES20.GL_FLOAT,
//                    false,
//                    0,
//                    dummyBuffer
//                )
//                GLES20.glVertexAttribPointer(
//                    GLES.texcoordHandle,
//                    2,
//                    GLES20.GL_FLOAT,
//                    false,
//                    0,
//                    dummyBuffer
//                )
//
//                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture0")
//
//                //無変換の記述はここ
//                GLES.disableShading()
//                GLES.enableTexture()
//                GLES.setPMatrix(projmtx)
//
//                GLES.setCMatrix(viewmtx)
//
//                //大きい地球の最前面にHelloを表示
//                Matrix.setIdentityM(mMatrix, 0)
//
//                val aMatrix = FloatArray(16) //モデル変換マトリックス
////                Matrix.setIdentityM(aMatrix, 0)
//                mAnchor!!.pose.toMatrix(aMatrix, 0)
//
//                Matrix.translateM(
//                    mMatrix,
//                    0,
//                    l.getPoints()[0].x,
//                    l.getPoints()[0].y,
//                    l.getPoints()[0].z
//                )
////                Matrix.translateM(mMatrix, 0, 0.1f, 0f, 0f)
//                Matrix.scaleM(mMatrix, 0, 0.1f, 0.1f, 0.1f)
//                ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "before set texture1")
//
//                GLES.updateMatrix(mMatrix, aMatrix)
//                StringTexture.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)
//            }

//            if(StrokeProvider.mStrokes.size > 0){
            if(true){
                GLES.useProgram()

                val dummyFloat = FloatArray(1)
                val dummyBuffer = BufferUtil.makeFloatBuffer(dummyFloat)
                //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
                GLES20.glVertexAttribPointer(
                    GLES.positionHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    dummyBuffer
                )
                GLES20.glVertexAttribPointer(
                    GLES.normalHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    dummyBuffer
                )
                GLES20.glVertexAttribPointer(
                    GLES.texcoordHandle,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    dummyBuffer
                )



                GLES.disableShading()
                GLES.enableTexture()
                //変換マトリックス
                val mMatrix = FloatArray(16) //モデル変換マトリックス
                val aMatrix = FloatArray(16) //モデル変換マトリックス
                Matrix.setIdentityM(aMatrix, 0)
                Matrix.setIdentityM(mMatrix, 0)

                GLES.setPMatrix(mMatrix)
                GLES.setCMatrix(mMatrix)

                Matrix.setIdentityM(mMatrix, 0)
                Matrix.translateM(mMatrix, 0, 0.5f, 0.1f, -0.3f)
                GLES.updateMatrix(mMatrix, aMatrix)
                TrianglTexture.draw(0.5f, .1f, 1.0f, 0.5f, 0.0f)
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

}