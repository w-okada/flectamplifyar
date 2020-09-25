package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.amplifyframework.core.Amplify
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.load_marker_view.view.*
import java.io.File

class LoadMarkerView: ConstraintLayout {
    private var bm: Bitmap? = null
    private var selectedLoadMarkerSpinnerItemState:LoadMarkerSpinnerItemState? = null

    companion object {
        private val TAG: String = LoadMarkerView::class.java.getSimpleName()
    }
    private lateinit var rootLayout:ConstraintLayout

    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.load_marker_view, this, true)

        loadMarkerListView.setOnItemClickListener{ adapterView, view, i, l ->
            //Log.e("-----", "${i} ${l} ${adapterView.adapter.getItem(i)}")
            val state = adapterView.adapter.getItem(i) as LoadMarkerSpinnerItemState
            loadMarkerImageView.setImageBitmap(state.bm)
            loadMarkerTextView.text = "${state.name}[${state.score}]"
            loadMarkerSelectButton.visibility = View.VISIBLE
            selectedLoadMarkerSpinnerItemState = state
        }


        loadMarkerSelectButton.setOnClickListener{
            arFragment!!.arOperationListener!!.setCurrentMarker(
                selectedLoadMarkerSpinnerItemState!!.id,
                {message ->
                    Log.e(ImageCaptureView.TAG,"set marker success. ${message}")
                },
                {message ->
                    Log.e(ImageCaptureView.TAG,"set marker failed. ${message}")
                }
            )
            arFragment!!.refreshImageDatabase(selectedLoadMarkerSpinnerItemState!!.bm!!)
            loadMarkerView.visibility = View.INVISIBLE
            arFragment!!.main_marker_view.setImageBitmap(selectedLoadMarkerSpinnerItemState!!.bm!!)
        }

        loadMarkerBackButton.setOnClickListener{
            loadMarkerView.visibility = View.INVISIBLE
        }

        loadMarkerRefreshButton.setOnClickListener{
            generateListView()
        }

    }

    var arFragment:ARFragment?=null

    fun generateListView(){
        arFragment!!.arOperationListener!!.getMarkers(
            {markers->
                val tmpList = ArrayList<LoadMarkerSpinnerItemState>()
                Log.e(TAG, "ARFRAGMENT::: ${arFragment}")
                arFragment!!.arOperationListener?.getMarkers(
                    {markers ->
                        Log.e(TAG, "MARKER ${markers}")
                        for(marker in markers){
                            val key = marker.path
                            val dstFile = File("${App.getApp().applicationContext.filesDir.toString()}/${marker.id}.jpg")
                            Amplify.Storage.downloadFile(
                                key,
                                dstFile,
                                { result ->
                                    Log.e("MyAmplifyApp", "Successfully downloaded: ${result.getFile().name}")
                                    val bm = BitmapFactory.decodeFile(dstFile.path)
                                    val state = LoadMarkerSpinnerItemState(marker.id, marker.name, marker.path, marker.score, bm)
                                    tmpList.add(state)

                                    val adapter = LoadMarkersSpinnerAdapter(context, 0,tmpList)
                                    loadMarkerListView.adapter=adapter
                                },
                                { error -> Log.e("MyAmplifyApp", "Download Failure", error) }
                            )
                            Log.e("---", "LIST MARKER4 marker ${marker}")
                        }
                    },
                    {message ->
                        Log.e(TAG, "getMarkers failed! ${message}")
                    }
                )
            },
            {message ->
                Log.e(TAG, "generate List view error  ${message}")
            }
        )

    }

}



data class LoadMarkerSpinnerItemState(var id:String="", var name: String = "", var imageURL: String = "", var score: Int = 0, var bm: Bitmap? = null)
data class LoadMarkerSpinnerViewHolder(var imageView: ImageView, var textView: TextView, var state:LoadMarkerSpinnerItemState)

class LoadMarkersSpinnerAdapter(private val mContext: Context, resource: Int, private val listState: ArrayList<LoadMarkerSpinnerItemState>) : ArrayAdapter<LoadMarkerSpinnerItemState>(mContext, resource, listState) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val holder: LoadMarkerSpinnerViewHolder
        if (convertView == null) {
            val layoutInflator = LayoutInflater.from(mContext)
            convertView = layoutInflator.inflate(R.layout.marker_spinner, null)
            holder = LoadMarkerSpinnerViewHolder(
                convertView.findViewById(R.id.marker_image_view),
                convertView.findViewById(R.id.marker_text_view),
                listState[position]
            )
            convertView.setTag(holder)
        } else {
            holder = convertView.getTag() as LoadMarkerSpinnerViewHolder
        }


        holder.imageView.setImageBitmap(listState[position].bm)
        holder.textView.text = "${listState[position].name} \n[score:${listState[position].score}]"
        return convertView!!
    }
}