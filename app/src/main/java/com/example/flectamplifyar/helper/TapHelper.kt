package com.example.flectamplifyar.helper

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

object TapHelper: View.OnTouchListener{

    lateinit private var gestureDetector: GestureDetector
    private val queuedSingleTaps: BlockingQueue<MotionEvent> = ArrayBlockingQueue(16)

    fun setup(context:Context){
        gestureDetector = GestureDetector(
            context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    queuedSingleTaps.offer(e)
                    return true
                }
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }
            })
    }


    fun poll(): MotionEvent? {
        return queuedSingleTaps.poll()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        queuedSingleTaps.offer(motionEvent)
        return true
    }

}
