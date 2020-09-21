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
import android.widget.ListView
import android.widget.TextView
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Marker
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection


//// 使った結果Spinnerではなかった。
//class LoadMarkerSpinner: androidx.appcompat.widget.AppCompatSpinner {
//
//    constructor(context: Context) : this(context, null){}
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        queryMarkers()
//    }
//
//    fun queryMarkers():ArrayList<LoadMarkerSpinnerItemState>{
//        val tmpList = ArrayList<LoadMarkerSpinnerItemState>()
//        Amplify.API.query(
//            ModelQuery.list(Marker::class.java, Marker.SCORE.gt(20)),
//            { response ->
//                for (marker in response.data) {
//                    val key = marker.path
//                    val dstFile = File("${App.getApp().applicationContext.filesDir.toString()}/${marker.id}.jpg")
//                    Log.e("---", "file: ${dstFile.absolutePath.toString()}")
//                    Amplify.Storage.downloadFile(
//                        key,
//                        dstFile,
//                        { result ->
//                            Log.i("MyAmplifyApp", "Successfully downloaded: ${result.getFile().name}")
//                            val bm = BitmapFactory.decodeFile(dstFile.path)
//                            val state = LoadMarkerSpinnerItemState(marker.id, marker.name, marker.path, marker.score, bm)
//                            tmpList.add(state)
//                            adapter = LoadMarkersSpinnerAdapter(context, 0, tmpList)
//                        },
//                        { error -> Log.e("MyAmplifyApp", "Download Failure", error) }
//                    )
//                    Log.i("MyAmplifyApp", "marker ${marker}")
//                }
//            },
//            { error -> Log.e("MyAmplifyApp", "Query failure", error) }
//        )
//        return tmpList
//    }
//
//    private fun getImageBitmap(url: String): Bitmap? {
//        var bm: Bitmap? = null
//        try {
//            val aURL = URL(url)
//            val conn: URLConnection = aURL.openConnection()
//            conn.connect()
//            val `is`: InputStream = conn.getInputStream()
//            val bis = BufferedInputStream(`is`)
//            bm = BitmapFactory.decodeStream(bis)
//            bis.close()
//            `is`.close()
//        } catch (e: IOException) {
//            Log.e("^^^^", "Error getting bitmap", e)
//        }
//        return bm
//    }
//}

class LoadMarkerListView:  ListView{

    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        queryMarkers()
    }



    private fun getMarkerOnlyRequest(): GraphQLRequest<Array<Marker>> {
//        val document = (
//                "query getMarker() { "
//                    + "getMarker(){ "
//                        + "id "
//                        + "score "
//                        + "name "
//                        + "path "
//                    + "} "
//                + "} "
//                )

        val document = (
                "query MyQuery { listMarkersa  {items{id name}}}"
//                "query MyQuery {\n" +
//                        "  listMarkers {\n" +
//                        "    items {\n" +
//                        "      name\n" +
//                        "    }\n" +
//                        "  }\n" +
//                        "}\n"

//                "query MyQuery {\n" +
//                        "  getMarker(id: \"0038ea5a34b02f46e577cf3fd5584be88e3e3c00\") {\n" +
//                        "    name\n" +
//                        "  }\n" +
//                        "}\n"
                )

        return SimpleGraphQLRequest(
            document,
            Marker::class.java,
            GsonVariablesSerializer()
        )
    }


    fun queryMarkers():ArrayList<LoadMarkerSpinnerItemState>{
        val tmpList = ArrayList<LoadMarkerSpinnerItemState>()
//        val request = getMarkerOnlyRequest()
//        Log.e("----------","REQUEST ${request.query}")
        Amplify.API.query(
            ModelQuery.list(Marker::class.java, Marker.SCORE.gt(20)),
//            request,
            { response ->
                Log.e("---", "LIST MARKER1: ${response}")
                for (marker in response.data) {
                    Log.e("---", "LIST MARKER2: ${marker}, ${marker.canvases.size}")
                    for(canvas in marker.canvases){
                        Log.e("---", "LIST MARKER3: ${canvas}")
                    }

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
                            adapter = LoadMarkersSpinnerAdapter(context, 0, tmpList)
                        },
                        { error -> Log.e("MyAmplifyApp", "Download Failure", error) }
                    )
                    Log.e("---", "LIST MARKER4 marker ${marker}")
                }
            },
            { error -> Log.e("MyAmplifyApp", "Query failure", error) }
        )
        return tmpList
    }

    private fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        try {
            val aURL = URL(url)
            val conn: URLConnection = aURL.openConnection()
            conn.connect()
            val `is`: InputStream = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e("^^^^", "Error getting bitmap", e)
        }
        return bm
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