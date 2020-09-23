package com.example.flectamplifyar.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*


class OverlayView: View {
    private val callbacks = LinkedList<DrawCallback>()
    private var ratioWidth = 0
    private var ratioHeight = 0

    constructor(context: Context) : super(context){}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    /**
     * Interface defining the callback for client classes.
     */
    interface DrawCallback {
        fun drawCallback(canvas: Canvas)
    }

    fun addCallback(callback: DrawCallback) {
        callbacks.add(callback)
    }

    @SuppressLint("MissingSuperCall")
    @Synchronized
    override fun onDraw(canvas: Canvas) {
        for (callback in callbacks) {
            callback.drawCallback(canvas)
        }
    }

    fun setAspectRatio(width: Int, height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }
    }

}