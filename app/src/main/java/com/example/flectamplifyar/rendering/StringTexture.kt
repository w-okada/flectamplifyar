package com.example.flectamplifyar.rendering

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Tommy on 2015/07/15.
 */
object StringTexture {
    private var TextureId = -1
    private var TextureUnitNumber = 0

    fun setup(text: String, textSize: Float, txtcolor: Int, bkcolor: Int) {
        makeStringTexture(text, textSize, txtcolor, bkcolor)
        setRectangular(1f, 1f)
    }

    fun makeStringTexture(text: String, textSize: Float, txtcolor: Int, bkcolor: Int) {

        // Text Bitmap生成
        val paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC))
        paint.textSize = textSize
        paint.isAntiAlias = true
        val fontMetrics = paint.fontMetrics

        var textWidth = paint.measureText(text).toInt()
        var textHeight = (Math.abs(fontMetrics.top) + fontMetrics.bottom).toInt()
        if (textWidth == 0) textWidth = 10
        if (textHeight == 0) textHeight = 10

        var bitmapsize = 2 //現時点でNexus7ではビットマップは正方形で一辺の長さは2のべき乗でなければならない
        while (bitmapsize < textWidth) bitmapsize *= 2
        while (bitmapsize < textHeight) bitmapsize *= 2
        val bitmap = Bitmap.createBitmap(bitmapsize, bitmapsize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paint.color = bkcolor
        canvas.drawRect(Rect(0, 0, bitmapsize, bitmapsize), paint)
        paint.color = txtcolor
        canvas.drawText(
            text,
            bitmapsize / 2 - textWidth / 2.toFloat(),
            bitmapsize / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2,
            paint
        )


        // Texture アップロード
        val firstIndex = 0
        val defaultOffset = 0
        val textures = IntArray(1)
        if (TextureId != -1) {
            textures[firstIndex] = TextureId
            GLES20.glDeleteTextures(1, textures, defaultOffset)
        }
        GLES20.glGenTextures(1, textures, defaultOffset)
        Log.e("-------------------","STRING TEXTURE ID = ${textures[0]}")

        TextureId = textures[firstIndex]
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+TextureUnitNumber);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        bitmap.recycle()
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }


    //bufferの定義
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ByteBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var texcoordBuffer: FloatBuffer? = null


    //頂点座標番号列
    private val indexs = byteArrayOf(
        0, 2, 1, 3
    )

    //拡頂点の法線ベクトル
    private val normals = floatArrayOf(
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f
    )
    var textcoords = floatArrayOf(
        1f, 1f, //右下 3
        0f, 1f,  //左下 2
        1f, 0f,  //右上 1
        0f, 0f,  //左上 0
    )


    fun setRectangular(width: Float, height: Float) {
        val top = height * .5f
        val bottom = -top
        val right = width * .5f
        val left = -right

        //頂点座標
        val vertexs = floatArrayOf(
            left, top, 0f,  //左上 0
            right, top, 0f,  //右上 1
            left, bottom, 0f,  //左下 2
            right, bottom, 0f, //右下 3
        )
        vertexBuffer = BufferUtil.makeFloatBuffer(vertexs)
        indexBuffer = BufferUtil.makeByteBuffer(indexs)
        normalBuffer = BufferUtil.makeFloatBuffer(normals)
        texcoordBuffer = BufferUtil.makeFloatBuffer(textcoords)
    }

    fun draw(r: Float, g: Float, b: Float, a: Float, shininess: Float) {
        //頂点点列のテクスチャ座標
        GLES20.glVertexAttribPointer(GLES.texcoordHandle, 2, GLES20.GL_FLOAT, false, 0, texcoordBuffer)

        //頂点点列
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        //頂点での法線ベクトル
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        //周辺光反射
        GLES20.glUniform4f(GLES.materialAmbientHandle, r, g, b, a)

        //拡散反射
        GLES20.glUniform4f(GLES.materialDiffuseHandle, r, g, b, a)

        //鏡面反射
        GLES20.glUniform4f(GLES.materialSpecularHandle, 1f, 1f, 1f, a)
        GLES20.glUniform1f(GLES.materialShininessHandle, shininess)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLES20.glUniform1i(GLES.textureHandle, TextureUnitNumber) //テクスチャユニット番号を指定する
        ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "set Texture4")


        // For occlusion
        // Attach the depth texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES.depthTextureId)
        GLES20.glUniform1i(GLES.depthTextureUniform, 1) //※２つ目
        // Set the depth texture uv transform.
        GLES20.glUniformMatrix3fv(GLES.depthUvTransformUniform, 1, false, GLES.uvTransform, 0)
        GLES20.glUniform1f(GLES.depthAspectRatioUniform, GLES.depthAspectRatio)

        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        GLES20.glUniform4f(GLES.objectColorHandle, r, g, b, a)

        //
        indexBuffer!!.position(0)


        GLES20.glEnableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glEnableVertexAttribArray(GLES.positionHandle)
        GLES20.glEnableVertexAttribArray(GLES.normalHandle)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP,
            4, GLES20.GL_UNSIGNED_BYTE, indexBuffer
        )
        GLES20.glDisableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glDisableVertexAttribArray(GLES.positionHandle)
        GLES20.glDisableVertexAttribArray(GLES.normalHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    }



}