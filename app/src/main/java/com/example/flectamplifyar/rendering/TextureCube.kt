package com.example.flectamplifyar.rendering

import android.content.Context
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
object TextureCube:TextureObject() {
//    private var TextureId = -1

    val TAG = TextureCube::class.java.simpleName

    fun setup(){
        setCube()
    }

    //bufferの定義
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ByteBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var texcoordBuffer: FloatBuffer? = null

    private val vertexs = floatArrayOf(
        1f,1f,1f,//P0
        1f,1f,-1f,//P1
        -1f,1f,1f,//P2
        -1f,1f,-1f,//P3
        -1f,1f,1f,//P4
        -1f,1f,-1f,//P5
        -1f,-1f,1f,//P6
        -1f,-1f,-1f,//P7
        -1f,-1f,1f,//P8
        -1f,-1f,-1f,//P9
        1f,-1f,1f,//P10
        1f,-1f,-1f,//P11
        1f,-1f,1f,//P12
        1f,-1f,-1f,//P13
        1f,1f,1f,//P14
        1f,1f,-1f,//P15
        -1f,-1f,1f,//P16
        1f,-1f,1f,//P17
        -1f,1f,1f,//P18
        1f,1f,1f,//P19
        1f,-1f,-1f,//P20
        -1f,-1f,-1f,//P21
        1f,1f,-1f,//P22
        -1f,1f,-1f//P23
    )

    //頂点座標番号列
    private val indexs = byteArrayOf(
        0,1,2,3, 4,5,6,7,
        8,9,10,11, 12,13,14,15, 15,16,
        16,17,18,19, 19,20, 20,21,22,23
    )

    //拡頂点の法線ベクトル
    private val normals = floatArrayOf(
        0f,1f,0f,  //P0
        0f,1f,0f,  //P1
        0f,1f,0f,  //P2
        0f,1f,0f,  //P3
        -1f,0f,0f,  //P4
        -1f,0f,0f,  //P5
        -1f,0f,0f,  //P6
        -1f,0f,0f,  //P7
        0f,-1f,0f,  //P8
        0f,-1f,0f,  //P9
        0f,-1f,0f,  //P10
        0f,-1f,0f,  //P11
        1f,0f,0f,  //P12
        1f,0f,0f,  //P13
        1f,0f,0f,  //P14
        1f,0f,0f,  //P15
        0f,0f,1f,  //P16
        0f,0f,1f,  //P17
        0f,0f,1f,  //P18
        0f,0f,1f,  //P19
        0f,0f,-1f,  //P20
        0f,0f,-1f,  //P21
        0f,0f,-1f,  //P22
        0f,0f,-1f  //P23
    )
    var textcoords = floatArrayOf(
        1f,0f, //face 0
        0f,0f,
        1f,1f,
        0f,1f,
        1f,0f, //face 1
        0f,0f,
        1f,1f,
        0f,1f,
        1f,0f, //face 2
        0f,0f,
        1f,1f,
        0f,1f,
        1f,0f, //face 3
        0f,0f,
        1f,1f,
        0f,1f,
        0f,1f, //face 4
        1f,1f,
        0f,0f,
        1f,0f,
        0f,1f, //face 5
        1f,1f,
        0f,0f,
        1f,0f
    )


    fun setCube() {
//        val top = height * .5f
//        val bottom = -top
//        val right = width * .5f
//        val left = -right

//        //頂点座標
//        val vertexs = floatArrayOf(
//            left, top, 0f,  //左上 0
//            right, top, 0f,  //右上 1
//            left, bottom, 0f,  //左下 2
//            right, bottom, 0f, //右下 3
//        )
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
        GLES20.glUniform1i(GLES.textureHandle, 0) //テクスチャユニット番号を指定する
        ShaderUtil.checkGLError(TAG, "set Texture4")


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
            indexs.size, GLES20.GL_UNSIGNED_BYTE, indexBuffer
        )
        GLES20.glDisableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glDisableVertexAttribArray(GLES.positionHandle)
        GLES20.glDisableVertexAttribArray(GLES.normalHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    }



}