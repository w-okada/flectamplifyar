package com.example.flectamplifyar.rendering

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


object BackgroundRenderer{
    private val TAG = BackgroundRenderer::class.java.simpleName
    // Shader names.
    private const val CAMERA_VERTEX_SHADER_NAME             = "shaders/screenquad_vert.glsl"
    private const val CAMERA_FRAGMENT_SHADER_NAME           = "shaders/screenquad_frag.glsl"
    private const val DEPTH_VISUALIZER_VERTEX_SHADER_NAME   = "shaders/background_show_depth_color_visualization_vert.glsl"
    private const val DEPTH_VISUALIZER_FRAGMENT_SHADER_NAME = "shaders/background_show_depth_color_visualization_frag.glsl"

    private const val COORDS_PER_VERTEX = 2
    private const val TEXCOORDS_PER_VERTEX = 2
    private const val FLOAT_SIZE = 4


    /**
     * (-1, 1) ------- (1, 1)
     * |    \           |
     * |       \        |
     * |          \     |
     * |             \  |
     * (-1, -1) ------ (1, -1)
     * Ensure triangles are front-facing, to support glCullFace().
     * This quad will be drawn using GL_TRIANGLE_STRIP which draws two
     * triangles: v0->v1->v2, then v2->v1->v3.
     */
    private val QUAD_COORDS = floatArrayOf(
        -1.0f, -1.0f, +1.0f, -1.0f, -1.0f, +1.0f, +1.0f, +1.0f
    )

    lateinit private var quadCoords: FloatBuffer
    lateinit private var quadTexCoords: FloatBuffer

    private var cameraProgram = 0
    private var depthProgram  = 0

    private var cameraPositionAttrib = 0
    private var cameraTexCoordAttrib = 0
    private var cameraTextureUniform = 0
    private var cameraTextureId = -1
    private var suppressTimestampZeroRendering = true

    private var depthPositionAttrib = 0
    private var depthTexCoordAttrib = 0
    private var depthTextureUniform = 0
    private var depthTextureId = -1


    // This Id is sent to the arcore via this function...
    fun getTextureId(): Int {
        return cameraTextureId
    }

    fun createOnGlThread(context: Context, depthTextureId: Int) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        cameraTextureId = textures[0]
        val textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES // for Image Stream
        GLES20.glBindTexture(textureTarget, cameraTextureId)
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        val numVertices = 4
        if (numVertices != QUAD_COORDS.size / COORDS_PER_VERTEX) {
            throw RuntimeException("Unexpected number of vertices in BackgroundRenderer.")
        }
        val bbCoords = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE)
        bbCoords.order(ByteOrder.nativeOrder())
        quadCoords = bbCoords.asFloatBuffer()
        quadCoords.put(QUAD_COORDS)
        quadCoords.position(0)
        val bbTexCoordsTransformed = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder())
        quadTexCoords = bbTexCoordsTransformed.asFloatBuffer()

        // Load render camera feed shader.
        run {
            val vertexShader   = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER,   CAMERA_VERTEX_SHADER_NAME)
            val fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, CAMERA_FRAGMENT_SHADER_NAME)
            cameraProgram = GLES20.glCreateProgram()
            GLES20.glAttachShader(cameraProgram, vertexShader)
            GLES20.glAttachShader(cameraProgram, fragmentShader)
            GLES20.glLinkProgram(cameraProgram)
            GLES20.glUseProgram(cameraProgram)
            cameraPositionAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_Position")
            cameraTexCoordAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_TexCoord")
            ShaderUtil.checkGLError(TAG, "Program creation")
            cameraTextureUniform = GLES20.glGetUniformLocation(cameraProgram, "sTexture")
            ShaderUtil.checkGLError(TAG, "Program parameters")
            Log.e("----","fin load background1")

        }

        // Load render depth map shader.
        run {
            val vertexShader   = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER,   DEPTH_VISUALIZER_VERTEX_SHADER_NAME)
            val fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, DEPTH_VISUALIZER_FRAGMENT_SHADER_NAME)
            depthProgram = GLES20.glCreateProgram()
            GLES20.glAttachShader(depthProgram, vertexShader)
            GLES20.glAttachShader(depthProgram, fragmentShader)
            GLES20.glLinkProgram(depthProgram)
            GLES20.glUseProgram(depthProgram)
            depthPositionAttrib = GLES20.glGetAttribLocation(depthProgram, "a_Position")
            depthTexCoordAttrib = GLES20.glGetAttribLocation(depthProgram, "a_TexCoord")
            ShaderUtil.checkGLError(TAG, "Program creation")
            depthTextureUniform = GLES20.glGetUniformLocation(depthProgram, "u_DepthTexture")
            ShaderUtil.checkGLError(TAG, "Program parameters")
            Log.e("----","fin load background2")

        }
        this.depthTextureId = depthTextureId
    }

    @Throws(IOException::class)
    fun createOnGlThread(context: Context) {
        createOnGlThread(context,  /*depthTextureId=*/-1)
    }

    fun suppressTimestampZeroRendering(suppressTimestampZeroRendering: Boolean) {
        this.suppressTimestampZeroRendering = suppressTimestampZeroRendering
    }



    fun draw(frame: Frame, debugShowDepthMap: Boolean) {
        // If display rotation changed (also includes view size change), we need to re-query the uv
        // coordinates for the screen rect, as they may have changed as well.
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadCoords,
                Coordinates2d.TEXTURE_NORMALIZED,
                quadTexCoords
            )
        }
        if (frame.timestamp == 0L && suppressTimestampZeroRendering) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            return
        }
        draw(debugShowDepthMap)
    }

    fun draw(frame: Frame) {
        draw(frame, false)
    }

    /**
     * Draws the camera image using the currently configured [BackgroundRenderer.quadTexCoords]
     * image texture coordinates.
     *
     *
     * The image will be center cropped if the camera sensor aspect ratio does not match the screen
     * aspect ratio, which matches the cropping behavior of [ ][Frame.transformCoordinates2d].
     */
    fun draw(imageWidth: Int, imageHeight: Int, screenAspectRatio: Float, cameraToDisplayRotation: Int) {
        // Crop the camera image to fit the screen aspect ratio.
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        val croppedWidth: Float
        val croppedHeight: Float
        if (screenAspectRatio < imageAspectRatio) {
            croppedWidth = imageHeight * screenAspectRatio
            croppedHeight = imageHeight.toFloat()
        } else {
            croppedWidth = imageWidth.toFloat()
            croppedHeight = imageWidth / screenAspectRatio
        }
        val u = (imageWidth - croppedWidth) / imageWidth * 0.5f
        val v = (imageHeight - croppedHeight) / imageHeight * 0.5f
        val texCoordTransformed: FloatArray
        texCoordTransformed = when (cameraToDisplayRotation) {
            90 -> floatArrayOf(1 - u, 1 - v, 1 - u, v, u, 1 - v, u, v)
            180 -> floatArrayOf(1 - u, v, u, v, 1 - u, 1 - v, u, 1 - v)
            270 -> floatArrayOf(u, v, u, 1 - v, 1 - u, v, 1 - u, 1 - v)
            0 -> floatArrayOf(u, 1 - v, 1 - u, 1 - v, u, v, 1 - u, v)
            else -> throw IllegalArgumentException("Unhandled rotation: $cameraToDisplayRotation")
        }

        // Write image texture coordinates.
        quadTexCoords.position(0)
        quadTexCoords.put(texCoordTransformed)
        draw( /*debugShowDepthMap=*/false)
    }


    /**
     * Draws the camera background image using the currently configured [ ][BackgroundRenderer.quadTexCoords] image texture coordinates.
     */
    private fun draw(debugShowDepthMap: Boolean) {
        // Ensure position is rewound before use.
        quadTexCoords.position(0)

        // No need to test or write depth, the screen quad has arbitrary depth, and is expected
        // to be drawn first.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        if (debugShowDepthMap) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureId)
            GLES20.glUseProgram(depthProgram)
            GLES20.glUniform1i(depthTextureUniform, 0)

            // Set the vertex positions and texture coordinates.
            GLES20.glVertexAttribPointer(
                depthPositionAttrib,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                quadCoords
            )
            GLES20.glVertexAttribPointer(
                depthTexCoordAttrib,
                TEXCOORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                quadTexCoords
            )
            GLES20.glEnableVertexAttribArray(depthPositionAttrib)
            GLES20.glEnableVertexAttribArray(depthTexCoordAttrib)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        } else {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId)
            GLES20.glUseProgram(cameraProgram)
            GLES20.glUniform1i(cameraTextureUniform, 0)

            // Set the vertex positions and texture coordinates.
            GLES20.glVertexAttribPointer(
                cameraPositionAttrib,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                quadCoords
            )
            GLES20.glVertexAttribPointer(
                cameraTexCoordAttrib,
                TEXCOORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                quadTexCoords
            )
            GLES20.glEnableVertexAttribArray(cameraPositionAttrib)
            GLES20.glEnableVertexAttribArray(cameraTexCoordAttrib)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        // Disable vertex arrays
        if (debugShowDepthMap) {
            GLES20.glDisableVertexAttribArray(depthPositionAttrib)
            GLES20.glDisableVertexAttribArray(depthTexCoordAttrib)
        } else {
            GLES20.glDisableVertexAttribArray(cameraPositionAttrib)
            GLES20.glDisableVertexAttribArray(cameraTexCoordAttrib)
        }

        // Restore the depth state for further drawing.
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        ShaderUtil.checkGLError(TAG, "BackgroundRendererDraw")
    }
}