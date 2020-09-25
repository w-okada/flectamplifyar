package com.example.flectamplifyar.dbmodel

import android.graphics.Color
import java.lang.reflect.Type
import javax.vecmath.Vector3f

data class DBObject(
    val uuid:String,
    val type:TYPE,
    val textureType:TEXTURE_TYPE,
    val color: Int,
    val text: String,
    val resId: Int,
    val locations :List<Vector3f>
) {
    enum class TYPE{
        LINE,
        RECT,
        TRIANGLE,
        SPHERE,
        CUBE,
    }
    enum class TEXTURE_TYPE{
        COLOR,
        STRING,
        IMAGE,
    }
}


