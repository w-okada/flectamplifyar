package com.example.flectamplifyar.model

import android.view.MotionEvent
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.vecmath.Vector3f

object StrokeProvider {
    val mStrokes: MutableList<Stroke> = ArrayList()

    fun addNewEvent(motionEvent:MotionEvent, newPoint:Vector3f){
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN ->{
                val stroke = Stroke()
                stroke.localLine = true
                stroke.setLineWidth(0.006f)
                stroke.add(newPoint)
                mStrokes.add(stroke)
            }
            MotionEvent.ACTION_MOVE ->{
                mStrokes[mStrokes.size-1].add(newPoint)
            }
            MotionEvent.ACTION_UP ->{
                mStrokes[mStrokes.size-1].add(newPoint)
            }
            else ->{}
        }
    }
}
