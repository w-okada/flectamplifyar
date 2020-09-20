package com.example.flectamplifyar.rendering

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.flectamplifyar.rendering.GLES
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Tommy on 2015/07/14.
 */
class Texture {
    private var TextureId = -1
    private var TextureUnitNumber = 0

    internal constructor(mContext: Context, id: Int, textureidnumber: Int) {
        TextureUnitNumber = textureidnumber
        makeTexture(mContext, id)
    }

    internal constructor(mContext: Context, id: Int) {
        makeTexture(mContext, id)
    }

    fun makeTexture(mContext: Context, id: Int) {
        val options = BitmapFactory.Options()
        options.inScaled = false
        //これをつけないとサイズが勝手に変更されてしまう
        //現時点でNexus7では正方形で一辺が2のべき乗サイズでなければならない
        //元のファイルの段階で大きさをそろえておく必要がある
        val bitmap = BitmapFactory.decodeResource(mContext.resources, id, options)
        val FIRST_INDEX = 0
        val DEFAULT_OFFSET = 0
        val textures = IntArray(1)
        if (TextureId != -1) {
            textures[FIRST_INDEX] = TextureId
            GLES20.glDeleteTextures(1, textures, DEFAULT_OFFSET)
        }
        GLES20.glGenTextures(1, textures, DEFAULT_OFFSET)
        TextureId = textures[FIRST_INDEX]
        //        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+TextureUnitNumber);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())
    }

    fun setTexture() {
        // テクスチャの指定
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TextureUnitNumber)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureId)
        GLES20.glUniform1i(GLES.textureHandle, TextureUnitNumber) //テクスチャユニット番号を指定する
    }
}