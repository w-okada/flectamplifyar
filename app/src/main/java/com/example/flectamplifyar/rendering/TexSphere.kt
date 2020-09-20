package com.example.flectamplifyar.rendering

import android.opengl.GLES20
import com.example.flectamplifyar.rendering.BufferUtil.makeFloatBuffer
import com.example.flectamplifyar.rendering.BufferUtil.makeShortBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by Tommy on 2015/07/11.
 */
object TexSphere {
    //bufferの定義
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var texcoordBuffer: FloatBuffer? = null
    private var nIndexs = 0

    init{
        makeTexSphere(20, 10)
    }

    fun makeTexSphere(nSlices: Int, nStacks: Int) {
        val Radius = 1f
        //頂点座標
        val nSlices1 = nSlices + 1
        val nPoints = (nStacks + 1) * nSlices1
        var sizeArray = nPoints * 3
        val vertexs = FloatArray(sizeArray)
        var i: Int
        var j: Int
        var px: Int
        var theta: Float
        var phi: Float
        i = 0
        while (i <= nStacks) {
            j = 0
            while (j < nSlices1) {
                px = (i * nSlices1 + j) * 3
                theta = (nStacks - i).toFloat() / nStacks.toFloat() * 3.14159265f - 3.14159265f * 0.5f
                phi = if (j == nSlices) {
                    0f
                } else {
                    j.toFloat() / nSlices.toFloat() * 2f * 3.14159265f
                }
                vertexs[px] = (Radius * Math.cos(theta.toDouble()) * Math.sin(phi.toDouble())).toFloat()
                vertexs[px + 1] = (Radius * Math.sin(theta.toDouble())).toFloat()
                vertexs[px + 2] = (Radius * Math.cos(theta.toDouble()) * Math.cos(phi.toDouble())).toFloat()
                j++
            }
            i++
        }

        //頂点座標番号列
        nIndexs = (nStacks + 1) * nSlices1 * 2 - (2 * nStacks + 4)
        val indexs = ShortArray(nIndexs)
        var p = 0
        i = 0
        while (i < nSlices) {
            if (p != 0) indexs[p++] = i.toShort()
            indexs[p++] = i.toShort()
            j = 1
            while (j < nStacks) {
                indexs[p++] = (j * nSlices1 + i).toShort()
                indexs[p++] = (j * nSlices1 + i + 1).toShort()
                j++
            }
            indexs[p++] = (nStacks * nSlices1 + i).toShort()
            if (p != nIndexs) indexs[p++] = (nStacks * nSlices1 + i).toShort()
            i++
        }
        sizeArray = nPoints * 2
        val textcoords = FloatArray(sizeArray)
        val dx = 1f / nSlices.toFloat()
        val dy = 1f / nStacks.toFloat()
        i = 0
        while (i <= nStacks) {
            j = 0
            while (j < nSlices1) {
                px = (i * nSlices1 + j) * 2
                textcoords[px++] = j * dx
                textcoords[px++] = i * dy
                j++
            }
            i++
        }
        vertexBuffer = makeFloatBuffer(vertexs)
        indexBuffer = makeShortBuffer(indexs)
        texcoordBuffer = makeFloatBuffer(textcoords)
    }

    fun draw(r: Float, g: Float, b: Float, a: Float, shininess: Float) {

        //頂点点列のテクスチャ座標
        GLES20.glVertexAttribPointer(
            GLES.texcoordHandle, 2,
            GLES20.GL_FLOAT, false, 0, texcoordBuffer
        )

        //頂点点列
        GLES20.glVertexAttribPointer(
            GLES.positionHandle, 3,
            GLES20.GL_FLOAT, false, 0, vertexBuffer
        )

        //頂点での法線ベクトル （これは頂点座標に等しい）
        GLES20.glVertexAttribPointer(
            GLES.normalHandle, 3,
            GLES20.GL_FLOAT, false, 0, vertexBuffer
        )

        //周辺光反射
        GLES20.glUniform4f(GLES.materialAmbientHandle, r, g, b, a)

        //拡散反射
        GLES20.glUniform4f(GLES.materialDiffuseHandle, r, g, b, a)

        //鏡面反射
        GLES20.glUniform4f(GLES.materialSpecularHandle, 1f, 1f, 1f, a)
        GLES20.glUniform1f(GLES.materialShininessHandle, shininess)

        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        GLES20.glUniform4f(GLES.objectColorHandle, 1f, 1f, 1f, a)

        //描画
        indexBuffer!!.position(0)

        GLES20.glEnableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glEnableVertexAttribArray(GLES.positionHandle)
        GLES20.glEnableVertexAttribArray(GLES.normalHandle)

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, nIndexs, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(GLES.texcoordHandle)
        GLES20.glDisableVertexAttribArray(GLES.positionHandle)
        GLES20.glDisableVertexAttribArray(GLES.normalHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    }
}