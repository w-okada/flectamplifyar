package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.load_marker_view.view.*
import kotlinx.android.synthetic.main.select_image_view.view.*

class SelectImageView:ConstraintLayout{

    companion object{
        val TAG = ImageCaptureView::class.java.simpleName
    }
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.select_image_view, this, true)
        select_image_grid_view.setOnItemClickListener{ adapterView, view, i, l ->
            arFragment.imageElementBitmap = (adapterView.adapter.getItem(i) as SelectImageGirdItemState).bm
            arFragment.imageElementBitmapId = (adapterView.adapter.getItem(i) as SelectImageGirdItemState).resId
            select_image_view.visibility = View.INVISIBLE
        }
    }

    lateinit var arFragment:ARFragment

    fun generateGird() {
        val arraylist = arrayListOf<SelectImageGirdItemState>(
            SelectImageGirdItemState(BitmapFactory.decodeResource(arFragment.resources, R.drawable.flect_logo_150x150), R.drawable.flect_logo_150x150),
            SelectImageGirdItemState(BitmapFactory.decodeResource(arFragment.resources, R.drawable.kurokawasan_150x150), R.drawable.kurokawasan_150x150) ,
        )
        val adapter = SelectImageGridAdapter(context, 0, arraylist)
        select_image_grid_view.adapter = adapter

    }
}

data class SelectImageGirdItemState(val bm: Bitmap?=null, val resId:Int)
data class SelectImageGirdItemViewHolder(var imageView: ImageView, var state:SelectImageGirdItemState)

class SelectImageGridAdapter(private val mContext: Context, resource: Int, private val listState: ArrayList<SelectImageGirdItemState>) : BaseAdapter() {

    override fun getCount(): Int {
        return listState.size
    }

    override fun getItem(p0: Int): Any {
        return listState[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val holder:SelectImageGirdItemViewHolder
        var tmpView = view
        if(tmpView == null){
            val layoutInflator = LayoutInflater.from(mContext)
            tmpView = layoutInflator.inflate(R.layout.select_image_view_item, null)

            Log.e("-----", "view null 1???? ${tmpView}")
            Log.e("-----", "view null 2???? ${tmpView.findViewById<ImageView>(R.id.select_image_view_item_image)}")

            holder = SelectImageGirdItemViewHolder(
                tmpView.findViewById(R.id.select_image_view_item_image),
                listState[position]
            )
            tmpView.setTag(holder)
        }else{
            holder = tmpView.getTag() as SelectImageGirdItemViewHolder
        }
        holder.imageView.setImageBitmap(listState[position].bm)
        return tmpView!!
    }


}