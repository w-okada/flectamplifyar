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
object TextureLine:TextureObject() {
    //buffer
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ByteBuffer? = null
    private var pointNum = 0

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



    }


    fun draw(r: Float, g: Float, b: Float, a: Float, shininess: Float, linewidth: Float) {
        //頂点点列
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)


        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        GLES20.glUniform4f(GLES.objectColorHandle, r, g, b, a)
        //線の太さ
        GLES20.glLineWidth(linewidth)


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLES20.glUniform1i(GLES.textureHandle, 0) //テクスチャユニット番号を指定する

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