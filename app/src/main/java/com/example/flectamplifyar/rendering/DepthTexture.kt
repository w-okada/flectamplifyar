package com.example.flectamplifyar.rendering

import android.opengl.GLES20
import android.opengl.GLES30
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException

object DepthTexture{
    private var textureId = -1
    private var width = -1
    private var height = -1

    fun createOnGlThread() {
        val textureIdArray = IntArray(1)
        GLES20.glGenTextures(1, textureIdArray, 0)
        textureId = textureIdArray[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    }

    fun updateWithDepthImageOnGlThread(frame: Frame) {
        try {
            val depthImage = frame.acquireDepthImage()
            width = depthImage.width
            height = depthImage.height
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RG8, width, height, 0,
                GLES30.GL_RG, GLES20.GL_UNSIGNED_BYTE, depthImage.planes[0].buffer)
            depthImage.close()
        } catch (e: NotYetAvailableException) {
            // This normally means that depth data is not available yet. This is normal so we will not
            // spam the logcat with this.
        }
    }
    fun getTextureId(): Int {
        return textureId
    }

    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }
}