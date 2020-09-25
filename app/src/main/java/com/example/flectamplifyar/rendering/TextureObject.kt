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
open class TextureObject {
    var TextureId = -1

    fun makeColorTexture(color: Int, size:Int =20){
        val paint = Paint()
        val bitmapsize = size
        val bitmap = Bitmap.createBitmap(bitmapsize, bitmapsize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paint.color = color
        canvas.drawRect(Rect(0, 0, bitmapsize, bitmapsize), paint)
        uploadTexture(bitmap)
        bitmap.recycle()
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

        uploadTexture(bitmap)
        bitmap.recycle()
    }


    fun makeImageTexture(mContext: Context, id: Int) {
        val options = BitmapFactory.Options()
        options.inScaled = false //これをつけないとサイズが勝手に変更されてしまう
        val bitmap = BitmapFactory.decodeResource(mContext.resources, id, options)
        uploadTexture(bitmap)
        bitmap.recycle()
    }
    fun makeImageTexture(bitmap: Bitmap) {
        uploadTexture(bitmap)
    }

    private fun uploadTexture(bitmap: Bitmap){
        // Texture アップロード
        val firstIndex = 0
        val defaultOffset = 0
        val textures = IntArray(1)
        if (TextureId != -1) { // 同じテクスチャIDを使い回す。(増えると落ちるため。　毎度生成し直すので性能は落ちると思われる)
            textures[firstIndex] = TextureId
            GLES20.glDeleteTextures(1, textures, defaultOffset)
        }
        GLES20.glGenTextures(1, textures, defaultOffset)

        TextureId = textures[firstIndex]
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())
    }
}