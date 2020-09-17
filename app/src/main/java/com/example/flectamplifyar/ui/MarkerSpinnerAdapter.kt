package com.example.flectamplifyar.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.marker_spinner.view.*


class MarkerSpinnerAdapter internal constructor(
    context: Context, spinnerItems: Array<String>, spinnerImages: Array<String>
) : BaseAdapter() {
    private val inflater: LayoutInflater
    private val names: Array<String>
    private val imageIDs: IntArray

    internal class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.marker_spinner, null)
            holder = ViewHolder()
            holder.imageView =convertView.image_view
            holder.textView = convertView.text_view
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
//        holder.imageView!!.setImageResource(imageIDs[position])
        holder.textView!!.text = names[position]
        return convertView
    }

    override fun getCount(): Int {
        return names.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    init {
        inflater = LayoutInflater.from(context)
        names = spinnerItems
        imageIDs = IntArray(spinnerImages.size)
        val res = context.resources

        // 最初に画像IDを配列で取っておく
        for (i in spinnerImages.indices) {
            imageIDs[i] = res.getIdentifier(
                spinnerImages[i],
                "drawable", context.packageName
            )
        }
    }
}