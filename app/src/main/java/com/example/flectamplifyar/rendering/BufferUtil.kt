package com.example.flectamplifyar.rendering

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by tommy on 2015/06/19.
 */
object BufferUtil {
    //convert float array to FloatBuffer
    fun makeFloatBuffer(array: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(array.size * 4).order(
            ByteOrder.nativeOrder()
        ).asFloatBuffer()
        fb.put(array).position(0)
        return fb
    }

    fun setFloatBuffer(array: FloatArray?, fb: FloatBuffer) {
        fb.put(array).position(0)
    }

    //convert byte array to ByteBuffer
    fun makeByteBuffer(array: ByteArray): ByteBuffer {
        val bb = ByteBuffer.allocateDirect(array.size).order(
            ByteOrder.nativeOrder()
        )
        bb.put(array).position(0)
        return bb
    }

    //convert short array to ShortBuffer
    fun makeShortBuffer(array: ShortArray): ShortBuffer {
        val sb = ByteBuffer.allocateDirect(array.size * 2).order(
            ByteOrder.nativeOrder()
        ).asShortBuffer()
        sb.put(array).position(0)
        return sb
    }
}