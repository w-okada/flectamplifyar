package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.load_marker_fragment.*
import kotlinx.android.synthetic.main.marker_spinner.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LoadMarkerFragment(): Fragment(){
    private var bm: Bitmap? = null
    private var selectedLoadMarkerSpinnerItemState:LoadMarkerSpinnerItemState? = null

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

        loadMarkerListView.setOnItemClickListener{ adapterView, view, i, l ->
            //Log.e("-----", "${i} ${l} ${adapterView.adapter.getItem(i)}")
            val state = adapterView.adapter.getItem(i) as LoadMarkerSpinnerItemState
            loadMarkerImageView.setImageBitmap(state.bm)
            loadMarkerTextView.text = "${state.name}[${state.score}]"
            loadMarkerSelectButton.visibility = View.VISIBLE
            selectedLoadMarkerSpinnerItemState = state
        }

        loadMarkerSelectButton.setOnClickListener{
            App.getApp().selectedMarkerBitmap = selectedLoadMarkerSpinnerItemState!!.bm
            App.getApp().selectedMarkerId = selectedLoadMarkerSpinnerItemState!!.id

            findNavController().navigate(R.id.action_second_to_first)
            setFragmentResult("loadMarkerFragmentResult", bundleOf(
                "selectedId" to selectedLoadMarkerSpinnerItemState!!.id,
            ))
        }

        loadMarkerBackButton.setOnClickListener{
            findNavController().navigate(R.id.action_second_to_first)
        }


        loadMarkerRefreshButton.setOnClickListener{
            loadMarkerListView.queryMarkers()
        }


//
    }


}
