package com.example.flectamplifyar.rendering

import android.opengl.GLES20
import android.util.Log
import com.example.flectamplifyar.rendering.BufferUtil.makeByteBuffer
import com.example.flectamplifyar.rendering.BufferUtil.makeFloatBuffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * Created by tommy on 2015/06/29.
 */
object TexRectangular {
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


    fun setup(){
        setRectangular(1f, 1f)
    }
    fun setup(width: Float, height: Float) {
        setRectangular(width, height)
    }

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
        vertexBuffer = makeFloatBuffer(vertexs)
        indexBuffer = makeByteBuffer(indexs)
        normalBuffer = makeFloatBuffer(normals)
        texcoordBuffer = makeFloatBuffer(textcoords)
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