package com.example.flectamplifyar.rendering

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.example.flectamplifyar.model.Stroke
import com.example.flectamplifyar.rendering.BufferUtil.makeByteBuffer
import com.example.flectamplifyar.rendering.BufferUtil.makeFloatBuffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10


// Lineを引くための参考として使用するつもりだったが、JustalineのLineがZ処理できるようになったのでとりあえずペンディング
/**
 * Created by Tommy on 2015/06/21.
 */
object Axes {
    //buffer
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ByteBuffer? = null
    private var pointNum = 0
    private var TextureId = -1
    private val vertexs = floatArrayOf( //  x, y, z
        -1f, 0f, 0f,  //x axis start     P0
        1f, 0f, 0f,  //P1
        0.9f, 0.05f, 0f,  //P2
        0.9f, 0f, 0f,  //P3
        0f, 0f, 0f,  //x axis end      //P4
        0f, -1f, 0f,  //y axis start   //P5
        0f, 1f, 0f,  //P6
        -0.05f, 0.9f, 0f,  //P7
        0f, 0.9f, 0f,  //P8
        0f, 0f, 0f,  //y axis end       P9
        0f, 0f, -1f,  //z axis start     P10
        0f, 0f, 1f,  //P11
        -0.05f, 0f, 0.9f,  //P12
        0f, 0f, 0.9f,  //z axis end     P13
        1.05f, 0f, 0f,  //char X         P14
        1.15f, 0.12f, 0f,  //P15
        1.1f, 0.06f, 0f,  //P16
        1.05f, 0.12f, 0f,  //P17
        1.15f, 0f, 0f,  //P18
        0.05f, 1.05f, 0f,  //char Y     //P19
        0.05f, 1.12f, 0f,  //P20
        0f, 1.17f, 0f,  //P21
        0.05f, 1.12f, 0f,  //P22
        0.1f, 1.17f, 0f,  //P23
        0.05f, 0.12f, 1.05f,  //char Z   P24
        0.1f, 0.12f, 1.05f,  //P25
        0.05f, 0f, 1.05f,  //P26
        0.1f, 0f, 1.05f //P27
    )

    //点の番号
    private val indexs = byteArrayOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        14, 15, 16, 17, 18,
        19, 20, 21, 22, 23,
        24, 25, 26, 27
    )

    init {
        makeAxes(1f)
    }


    fun makeAxes(size: Float) {
        var i: Int
        i = 0
        while (i < 28 * 3) {
            vertexs[i] *= size
            i++
        }
        vertexBuffer = makeFloatBuffer(vertexs)
        indexBuffer = makeByteBuffer(indexs)
    }



    fun makeAStroke(stroke: Stroke) {
        pointNum = stroke.size()
        val pointArray = FloatArray(pointNum*3)
        val indexArray = ByteArray(pointNum)
        for(i in 0..pointNum-1){
            val point = stroke.get(i)
            pointArray[i * 3    ] =  point.x
            pointArray[i * 3 + 1] =  point.y
            pointArray[i * 3 + 2] =  point.z
            indexArray[i] = i.toByte()
        }
        vertexBuffer = makeFloatBuffer(pointArray)
        indexBuffer = makeByteBuffer(indexArray)







        // Text Bitmap生成
        val paint = Paint()
        val bitmapsize = 20
        val bitmap = Bitmap.createBitmap(bitmapsize, bitmapsize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paint.color = Color.GREEN
        canvas.drawRect(Rect(0, 0, bitmapsize, bitmapsize), paint)

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        bitmap.recycle()
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())






    }


    fun draw(r: Float, g: Float, b: Float, a: Float, shininess: Float, linewidth: Float) {
        //頂点点列
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)


        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        GLES20.glUniform4f(GLES.objectColorHandle, r, g, b, a)
        //線の太さ
        GLES20.glLineWidth(linewidth)

        // For occlusion
        // Attach the depth texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES.depthTextureId)
        GLES20.glUniform1i(GLES.depthTextureUniform, 1) //※２つ目
        // Set the depth texture uv transform.
        GLES20.glUniformMatrix3fv(GLES.depthUvTransformUniform, 1, false, GLES.uvTransform, 0)
        GLES20.glUniform1f(GLES.depthAspectRatioUniform, GLES.depthAspectRatio)


        GLES20.glEnableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glEnableVertexAttribArray(GLES.positionHandle)
        GLES20.glEnableVertexAttribArray(GLES.normalHandle)

        //ここから描画
        //P0から14個の連続点で，座標軸を一気に描く（都合上同じ線を2度引くところもある）
        indexBuffer!!.position(0)
        GLES20.glDrawElements(GLES20.GL_LINE_STRIP, pointNum-1 , GLES20.GL_UNSIGNED_BYTE, indexBuffer)


        GLES20.glDisableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glDisableVertexAttribArray(GLES.positionHandle)
        GLES20.glDisableVertexAttribArray(GLES.normalHandle)

    }
}