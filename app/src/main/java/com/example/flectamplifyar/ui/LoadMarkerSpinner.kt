package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.image_capture_view.view.*
import java.io.File
import java.net.URI

class LoadMarkerSpinner: androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    fun setMarkerList(){
        val select_qualification = arrayOf(
            "Menuaaa", "Load Marker", "Capture Marker", "[]Show Pallet",
            "Menuaaa", "Load Marker", "Capture Marker", "[]Show Pallet",
            "Menuaaa", "Load Marker", "Capture Marker", "[]Show Pallet"
        )
        val states: ArrayList<LoadMarkerSpinnerItemState> = ArrayList()
        for (i in 0 until select_qualification.size) {
            val state = LoadMarkerSpinnerItemState(
                select_qualification[i], "URLaaa", 100000
            )
            states.add(state)
        }
        Log.e("asdf","SIZE!!! ${states.size}")
        queryMarkers()
        adapter = LoadMarkersSpinnerAdapter(context, 0, states)

    }



    private fun queryMarkers(){
        Amplify.API.query(
            ModelQuery.list(Marker::class.java, Marker.SCORE.gt(20)),
            { response ->
                for (marker in response.data) {
                    Log.i("MyAmplifyApp", "marker ${marker}")
                }
            },
            { error -> Log.e("MyAmplifyApp", "Query failure", error) }
        )
    }
}


data class LoadMarkerSpinnerItemState(var name: String = "", var imageURL: String = "", var score: Int = 0)
data class LoadMarkerSpinnerViewHolder(var imageView: ImageView, var textView: TextView)



class LoadMarkersSpinnerAdapter(private val mContext: Context, resource: Int, private val listState: ArrayList<LoadMarkerSpinnerItemState>) : ArrayAdapter<LoadMarkerSpinnerItemState>(mContext, resource, listState) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.e("------","DROPDOWN: ${position}")
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.e("------","VIEW: ${position}")
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
                convertView.findViewById(R.id.marker_text_view)
            )
            convertView.setTag(holder)
        } else {
            holder = convertView.getTag() as LoadMarkerSpinnerViewHolder
        }


        holder.imageView
        holder.textView.text = listState[position].name
        Log.e("-afdasdf","DROP:  ${position}, ${listState[position].name}")
        return convertView!!
    }
}