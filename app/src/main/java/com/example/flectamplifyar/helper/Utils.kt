package com.example.flectamplifyar.helper

import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream

//　！！！！！ RecordableSurfaceを使うため、不使用化

object Utils{
    fun generateBitmap(img: Image, rotate: Int): Bitmap {
        val y_size = img.planes[0].buffer.remaining()
        val u_size = img.planes[1].buffer.remaining()
        val v_size = img.planes[2].buffer.remaining()

        val ba = ByteArray(y_size + u_size + v_size)
        img.planes[0].buffer.get(ba, 0, y_size)

        val bau = ByteArray(u_size)
        val bav = ByteArray(v_size)
        img.planes[1].buffer.get(bau, 0, u_size)
        img.planes[2].buffer.get(bav, 0, v_size)
        img.planes[2].buffer.get(ba, y_size, u_size)
        img.planes[1].buffer.get(ba, y_size + u_size, v_size)

        val yimg = YuvImage(ba, ImageFormat.NV21, img.width, img.height, null)
        val out = ByteArrayOutputStream()
        yimg.compressToJpeg(Rect(0, 0, img.width, img.height), 100, out);
        val yba = out.toByteArray()
        val ybm = BitmapFactory.decodeByteArray(yba, 0, yba.size);


        val bm: Bitmap = when(rotate){
            90 -> {
                val mat = android.graphics.Matrix()
                mat.postRotate(rotate.toFloat())
                Bitmap.createBitmap(ybm, 0, 0, img.width, img.height, mat, true)
            }
            180 -> {
                val mat = android.graphics.Matrix()
                mat.postRotate(rotate.toFloat())
                Bitmap.createBitmap(ybm, 0, 0, img.width, img.height, mat, true)
            }
            else -> {
                ybm
            }
        }
        img.close()
        return bm
    }
}