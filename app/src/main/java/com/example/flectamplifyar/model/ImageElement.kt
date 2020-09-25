package com.example.flectamplifyar.model

import android.graphics.Bitmap
import javax.vecmath.Vector3f

data class ImageElement(val uuid:String, val resId:Int, val bitmap: Bitmap, val position: Vector3f)
