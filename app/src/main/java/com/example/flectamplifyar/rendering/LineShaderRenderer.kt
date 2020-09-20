package com.example.flectamplifyar.rendering


import android.content.Context
import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.example.flectamplifyar.model.Stroke
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.vecmath.Vector3f


/**
 * Renders a point cloud.
 */
object LineShaderRenderer {

    private const val LINE_VERTEX_SHADER_NAME             = "shaders/line_vert.glsl"
    private const val LINE_FRAGMENT_SHADER_NAME           = "shaders/line_frag.glsl"
    private val TAG = LineShaderRenderer::class.java.simpleName
    private const val FLOATS_PER_POINT = 3 // X,Y,Z.
    private const val BYTES_PER_FLOAT = 4
    private const val BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT


    open var mModelMatrix = FloatArray(16)
    private val mModelViewMatrix = FloatArray(16)
    private val mModelViewProjectionMatrix = FloatArray(16)
    private var mPositionAttribute = 0
    private var mPreviousAttribute = 0
    private var mNextAttribute = 0
    private var mSideAttribute = 0
    private var mWidthAttribute = 0
    private var mLengthsAttribute = 0
    private var mEndCapsAttribute = 0
    private val textures = IntArray(1)

    //private int mTextureUniform = 0;
    private var mEndCapTextureUniform = 0
    private var mProjectionUniform = 0
    private var mModelViewUniform = 0
    private var mResolutionUniform = 0
    private var mColorUniform = 0
    private var mNearUniform = 0
    private var mFarUniform = 0
    private var mDrawingDistUniform = 0
    private var mLineDepthScaleUniform = 0
    private var mPositions: FloatArray? = null
    private var mNext: FloatArray? = null
    private var mSide: FloatArray? = null
    private var mWidth: FloatArray? = null
    private var mPrevious: FloatArray? = null
    private var mLengths: FloatArray? = null
    private var mEndCaps: FloatArray? = null
    private var mPositionAddress = 0
    private var mPreviousAddress = 0
    private var mNextAddress = 0
    private var mSideAddress = 0
    private var mWidthAddress = 0
    private var mLengthAddress = 0
    private var mEndCapsAddress = 0
    private var mNumBytes = 0
    private var mVbo = 0
    private var mVboSize = 0
    private var mProgramName = 0
    private var mLineWidth = 0f
    private var mColor: Vector3f? = null
    var bNeedsUpdate = AtomicBoolean()
    private var mLineDepthScale = 1.0f
    var mDrawDistance = 0f
    var mNumPoints = 0


    @Throws(IOException::class)
    fun createOnGlThread(context: Context) {
        ShaderUtil.checkGLError(TAG, "before create")
        val buffers = IntArray(1)
        GLES20.glGenBuffers(1, buffers, 0)
        mVbo = buffers[0]

        Log.e("^^^^^^^^","------- mvbo:"+mVbo)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        mVboSize = 0
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            mVboSize,
            null,
            GLES20.GL_DYNAMIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        ShaderUtil.checkGLError(TAG, "buffer alloc")

        val vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, LINE_VERTEX_SHADER_NAME)
        val fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, LINE_FRAGMENT_SHADER_NAME)



        mProgramName = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgramName, vertexShader)
        GLES20.glAttachShader(mProgramName, fragmentShader)
        GLES20.glLinkProgram(mProgramName)
        GLES20.glUseProgram(mProgramName)
        ShaderUtil.checkGLError(TAG, "program")

        // mTextureUniform = GLES20.glGetUniformLocation(mProgramName, "u_Texture");
        mEndCapTextureUniform = GLES20.glGetUniformLocation(mProgramName, "u_EndCapTexture")
        mPositionAttribute = GLES20.glGetAttribLocation(mProgramName, "position")
        mPreviousAttribute = GLES20.glGetAttribLocation(mProgramName, "previous")
        mNextAttribute = GLES20.glGetAttribLocation(mProgramName, "next")
        mSideAttribute = GLES20.glGetAttribLocation(mProgramName, "side")
        mWidthAttribute = GLES20.glGetAttribLocation(mProgramName, "width")
        mLengthsAttribute = GLES20.glGetAttribLocation(mProgramName, "length")
        mEndCapsAttribute = GLES20.glGetAttribLocation(mProgramName, "endCaps")
        mProjectionUniform = GLES20.glGetUniformLocation(mProgramName, "projectionMatrix")
        mModelViewUniform = GLES20.glGetUniformLocation(mProgramName, "modelViewMatrix")
        mResolutionUniform = GLES20.glGetUniformLocation(mProgramName, "resolution")
        mColorUniform = GLES20.glGetUniformLocation(mProgramName, "color")
        mNearUniform = GLES20.glGetUniformLocation(mProgramName, "near")
        mFarUniform = GLES20.glGetUniformLocation(mProgramName, "far")
        mLineDepthScaleUniform = GLES20.glGetUniformLocation(mProgramName, "lineDepthScale")
        mDrawingDistUniform = GLES20.glGetUniformLocation(mProgramName, "drawingDist")
        ShaderUtil.checkGLError(TAG, "program  params")




        // Read the line texture.
        val endCapTextureBitmap = BitmapFactory.decodeStream(context.assets.open("linecap.png"))
        GLES20.glGenTextures(1, textures, 0)
        Log.e("-------------------","LINE TEXTURE ID = ${textures[0]}")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, endCapTextureBitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        ShaderUtil.checkGLError(TAG, "Line texture loading")
        Matrix.setIdentityM(mModelMatrix, 0)
        mColor = Vector3f(1f, 1f, 1f)
    }

    fun clearGL() {
        GLES20.glDeleteShader(mProgramName)
        GLES20.glDeleteBuffers(1, intArrayOf(mVbo), 0)
    }

    fun setLineWidth(width: Float) {
        mLineWidth = width
    }

    fun setColor(color: Vector3f?) {
        mColor = Vector3f(color)
    }

    fun setDistanceScale(distanceScale: Float) {
        mLineDepthScale = distanceScale
    }

    fun updateStrokes(strokes: List<Stroke>, sharedStrokes: Map<String, Stroke>) {
//        Log.e("------------","updateStrokes!!!1 : local: ${strokes.size} shared: ${sharedStrokes.size}")
//        Log.e("------------","updateStrokes!!!1 : local: ${strokes.size} shared: ${sharedStrokes.values.size}")
        mNumPoints = 0
        for (l in strokes) {
            mNumPoints += l.size() * 2 + 2
        }
        for (l in sharedStrokes.values) {
            mNumPoints += l.size() * 2 + 2
        }
        ensureCapacity(mNumPoints)
        var offset = 0
        for (l in strokes) {
//            Log.e("-------------","STROKE COUNT LOCAL")
            offset = addLine(l, offset)
        }
        for (l in sharedStrokes.values) {
//            Log.e("-------------","STROKE COUNT SHARED")
            offset = addLine(l, offset)
        }
        mNumBytes = offset
//        Log.e("------------","updateStrokes!!!2 : offset: ${offset} point: ${mNumPoints}")
    }

    /**
     * This ensures the capacity of the float arrays that hold the information bound to the Vertex
     * Attributes needed to render the line with the Vertex and Fragment shader.
     *
     * @param numPoints int denoting number of points
     */
    private fun ensureCapacity(numPoints: Int) {
        var count = 1024
        if (mSide != null) {
            count = mSide!!.size
        }
        while (count < numPoints) {
            count += 1024
        }
        if (mSide == null || mSide!!.size < count) {
            Log.e(TAG, "ensureCapacity alloc $count")
            mPositions = FloatArray(count * 3)
            mNext = FloatArray(count * 3)
            mPrevious = FloatArray(count * 3)
            mSide = FloatArray(count)
            mWidth = FloatArray(count)
            mLengths = FloatArray(count)
            mEndCaps = FloatArray(count)
        }
    }

    /**
     * AddLine takes in the 3D positions adds to the buffers to create the stroke and the degenerate
     * faces needed so the lines render properly.
     */
    private fun addLine(line: Stroke?, offset: Int): Int {

        if (line == null || line.size() < 2){
            return offset
        }
        val lineSize: Int = line.size()
        mLineWidth = line.getLineWidth()
        val mLineWidthMax = mLineWidth
        var length = 0f
        val totalLength: Float
        var ii = offset
        totalLength = if (line.localLine) {
            line.totalLength
        } else {
            line.animatedLength
        }
        for (i in 0 until lineSize) {
            var iGood = i
            if (iGood >= lineSize) iGood = lineSize - 1
            val i_m_1 = if (iGood - 1 < 0) iGood else iGood - 1
            val i_p_1 = if (iGood + 1 > lineSize - 1) iGood else iGood + 1
            val current: Vector3f = line.get(iGood)
            val previous: Vector3f = line.get(i_m_1)
            val next: Vector3f = line.get(i_p_1)
            val dist = Vector3f(current)
            dist.sub(previous)
            length += dist.length()


//            if (i < line.mTapperPoints) {
//                mLineWidth = mLineWidthMax * line.mTaperLookup[i];
//            } else if (i > lineSize - line.mTapperPoints) {
//                mLineWidth = mLineWidthMax * line.mTaperLookup[lineSize - i];
//            } else {
            mLineWidth = line.getLineWidth()
            //            }
            mLineWidth = Math.max(0f, Math.min(mLineWidthMax, mLineWidth))
            if (i == 0) {
                setMemory(ii++, current, previous, next, mLineWidth, 1f, length, totalLength)
            }
            setMemory(ii++, current, previous, next, mLineWidth, 1f, length, totalLength)
            setMemory(ii++, current, previous, next, mLineWidth, -1f, length, totalLength)
            if (i == lineSize - 1) {
                setMemory(ii++, current, previous, next, mLineWidth, -1f, length, totalLength)
            }
        }

        return ii
    }

    /**
     * setMemory is a helper method used to add the stroke data to the float[] buffers
     */
    private fun setMemory(index: Int, pos: Vector3f, prev: Vector3f, next: Vector3f,
                          width: Float, side: Float, length: Float, endCapPosition: Float) {
        mPositions!![index * 3] = pos.x
        mPositions!![index * 3 + 1] = pos.y
        mPositions!![index * 3 + 2] = pos.z
        mNext!![index * 3] = next.x
        mNext!![index * 3 + 1] = next.y
        mNext!![index * 3 + 2] = next.z
        mPrevious!![index * 3] = prev.x
        mPrevious!![index * 3 + 1] = prev.y
        mPrevious!![index * 3 + 2] = prev.z
        mSide!![index] = side
        mWidth!![index] = width
        mLengths!![index] = length
        mEndCaps!![index] = endCapPosition
    }

    /**
     * Sets the bNeedsUpdate to true.
     */
    fun clear() {
        bNeedsUpdate.set(true)
    }

    /**
     * This takes the float[] and creates FloatBuffers, Binds the VBO, and upload the Attributes to
     * correct locations with the correct offsets so the Vertex and Fragment shader can render the lines
     */
    fun upload() {
        bNeedsUpdate.set(false)
        val current = toFloatBuffer(mPositions)
        val next = toFloatBuffer(mNext)
        val previous = toFloatBuffer(mPrevious)
        val side = toFloatBuffer(mSide)
        val width = toFloatBuffer(mWidth)
        val lengths = toFloatBuffer(mLengths)
        val endCaps = toFloatBuffer(mEndCaps)


//        mNumPoints = mPositions.length;
        mPositionAddress = 0
        mNextAddress = mPositionAddress + mNumBytes * 3 * BYTES_PER_FLOAT
        mPreviousAddress = mNextAddress + mNumBytes * 3 * BYTES_PER_FLOAT
        mSideAddress = mPreviousAddress + mNumBytes * 3 * BYTES_PER_FLOAT
        mWidthAddress = mSideAddress + mNumBytes * BYTES_PER_FLOAT
        mLengthAddress = mWidthAddress + mNumBytes * BYTES_PER_FLOAT
        mEndCapsAddress = mLengthAddress + mNumBytes * BYTES_PER_FLOAT
        mVboSize = mEndCapsAddress + mNumBytes * BYTES_PER_FLOAT
        ShaderUtil.checkGLError(TAG, "before update")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVboSize, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mPositionAddress, mNumBytes * 3 * BYTES_PER_FLOAT, current)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mNextAddress, mNumBytes * 3 * BYTES_PER_FLOAT, next)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mPreviousAddress, mNumBytes * 3 * BYTES_PER_FLOAT, previous)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mSideAddress, mNumBytes * BYTES_PER_FLOAT, side)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mWidthAddress, mNumBytes * BYTES_PER_FLOAT, width)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mLengthAddress, mNumBytes * BYTES_PER_FLOAT, lengths)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mEndCapsAddress, mNumBytes * BYTES_PER_FLOAT, endCaps)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        ShaderUtil.checkGLError(TAG, "after update")
//        Log.e("---------------","OPEN GL UPLOADED")
    }

    /**
     * This method takes in the current CameraView Matrix and the Camera's Projection Matrix, the
     * current position and pose of the device, uses those to calculate the ModelViewMatrix and
     * ModelViewProjectionMatrix.  It binds the VBO, enables the custom attribute locations,
     * binds and uploads the shader uniforms, calls our single DrawArray call, and finally disables
     * and unbinds the shader attributes and VBO.
     */
    fun draw(cameraView: FloatArray?, cameraPerspective: FloatArray?, screenWidth: Float,
        screenHeight: Float, nearClip: Float, farClip: Float) {

        Matrix.multiplyMM(mModelViewMatrix, 0, cameraView, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, cameraPerspective, 0, mModelViewMatrix, 0)
        ShaderUtil.checkGLError(TAG, "Before draw")
        GLES20.glUseProgram(mProgramName)
//        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        // Blending setup
        GLES20.glEnable(GLES20.GL_BLEND)
        //        GLES20.glBlendFuncSeparate(
//                GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA, // RGB (src, dest)
//                GLES20.GL_ZERO, GLES20.GL_ONE); // ALPHA (src, dest)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

//         Attach the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        GLES20.glUniform1i(mEndCapTextureUniform, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo)


        GLES20.glVertexAttribPointer(mPositionAttribute, FLOATS_PER_POINT, GLES20.GL_FLOAT,
            false, BYTES_PER_POINT, mPositionAddress)

        GLES20.glVertexAttribPointer(mPreviousAttribute, FLOATS_PER_POINT, GLES20.GL_FLOAT,
            false, BYTES_PER_POINT, mPreviousAddress)

        GLES20.glVertexAttribPointer(mNextAttribute, FLOATS_PER_POINT, GLES20.GL_FLOAT,
            false, BYTES_PER_POINT, mNextAddress)

        GLES20.glVertexAttribPointer(mSideAttribute, 1, GLES20.GL_FLOAT,
            false, BYTES_PER_FLOAT, mSideAddress)

        GLES20.glVertexAttribPointer(mWidthAttribute, 1, GLES20.GL_FLOAT,
            false, BYTES_PER_FLOAT, mWidthAddress)

        GLES20.glVertexAttribPointer(mLengthsAttribute, 1, GLES20.GL_FLOAT,
            false, BYTES_PER_FLOAT, mLengthAddress)

        GLES20.glVertexAttribPointer(mEndCapsAttribute, 1, GLES20.GL_FLOAT,
            false, BYTES_PER_FLOAT, mEndCapsAddress)

        GLES20.glUniformMatrix4fv(mModelViewUniform, 1, false, mModelViewMatrix, 0)
        GLES20.glUniformMatrix4fv(mProjectionUniform, 1, false, cameraPerspective, 0)

        GLES20.glUniform2f(mResolutionUniform, screenWidth, screenHeight)
        GLES20.glUniform3f(mColorUniform, mColor!!.x, mColor!!.y, mColor!!.z)
        GLES20.glUniform1f(mNearUniform, nearClip)
        GLES20.glUniform1f(mFarUniform, farClip)
        GLES20.glUniform1f(mLineDepthScaleUniform, mLineDepthScale)
        GLES20.glUniform1f(mDrawingDistUniform, mDrawDistance)
        GLES20.glEnableVertexAttribArray(mPositionAttribute)
        GLES20.glEnableVertexAttribArray(mPreviousAttribute)
        GLES20.glEnableVertexAttribArray(mNextAttribute)
        GLES20.glEnableVertexAttribArray(mSideAttribute)
        GLES20.glEnableVertexAttribArray(mWidthAttribute)
        GLES20.glEnableVertexAttribArray(mLengthsAttribute)
        GLES20.glEnableVertexAttribArray(mEndCapsAttribute)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mNumBytes)
        GLES20.glDisableVertexAttribArray(mEndCapsAttribute)
        GLES20.glDisableVertexAttribArray(mLengthsAttribute)
        GLES20.glDisableVertexAttribArray(mWidthAttribute)
        GLES20.glDisableVertexAttribArray(mSideAttribute)
        GLES20.glDisableVertexAttribArray(mNextAttribute)
        GLES20.glDisableVertexAttribArray(mPreviousAttribute)
        GLES20.glDisableVertexAttribArray(mPositionAttribute)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
/////////        GLES20.glDisable(GLES20.GL_BLEND)  //　使わない
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    /**
     * A helper function to allocate a FloatBuffer the size of our float[] and copy the float[] into
     * the newly created FloatBuffer.
     */
    private fun toFloatBuffer(data: FloatArray?): FloatBuffer {
        val buff: FloatBuffer
        val bb =
            ByteBuffer.allocateDirect(data!!.size * BYTES_PER_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        buff = bb.asFloatBuffer()
        buff.put(data)
        buff.position(0)
        return buff
    }

}
