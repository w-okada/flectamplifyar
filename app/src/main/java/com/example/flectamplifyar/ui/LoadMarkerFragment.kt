package com.example.flectamplifyar.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.load_marker_fragment.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LoadMarkerFragment(): Fragment(), GLSurfaceView.Renderer{
    companion object {
        private val TAG: String = LoadMarkerFragment::class.java.getSimpleName()
    }
    private lateinit var rootLayout:ConstraintLayout

    // Lifecyle
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.fragment,0,0)
        val param = a.getString(R.styleable.fragment_param);
        a.recycle()
        Log.e(TAG, "Inflateing... LOAD")
        Log.e(TAG, "    LOADParam: ${param}")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        return inflater.inflate(R.layout.load_marker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMarkerSpinner.setMarkerList()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
//        TODO("Not yet implemented")
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
//        TODO("Not yet implemented")
    }

    override fun onDrawFrame(p0: GL10?) {
//        TODO("Not yet implemented")
    }

}
