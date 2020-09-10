package com.example.flectamplifyar.rendering

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Tommy on 2015/07/15.
 */
class StringTexture {
    private var TextureId = -1
    private var TextureUnitNumber = 0

    internal constructor(text: String, textSize: Float, txtcolor: Int, bkcolor: Int, textureidnumber: Int) {
        TextureUnitNumber = textureidnumber
        makeStringTexture(text, textSize, txtcolor, bkcolor)
    }

    internal constructor(text: String, textSize: Float, txtcolor: Int, bkcolor: Int) {
        makeStringTexture(text, textSize, txtcolor, bkcolor)
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

    fun setTexture() {
        // テクスチャの指定
//        Log.e("texture:", "TextureUnitNumber"+GLES20.GL_TEXTURE0 + TextureUnitNumber)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TextureUnitNumber)
//        Log.e("texture:", "TextureId"+TextureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLES20.glUniform1i(GLES.textureHandle, TextureUnitNumber) //テクスチャユニット番号を指定する
        ShaderUtil.checkGLError("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa", "set Texture4")
    }
}