package com.example.flectamplifyar.ui




///// 注意！！！　Spinnerはタイトルが設定できない、また、イベントのハンドルが難しいため不使用。このクラスは参考のために残す。







import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.flectamplifyar.R


class MenuSpinner: androidx.appcompat.widget.AppCompatSpinner {
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val select_qualification = arrayOf("Menu", "Load Marker", "Capture Marker", "[]Show Pallet")
        val states: ArrayList<MenuSpinnerItemState> = ArrayList()
        for (i in 0 until select_qualification.size) {
            val state = MenuSpinnerItemState()
            state.title = select_qualification[i]
            state.isSelected = false
            states.add(state)
        }
        setPrompt("Title");
        adapter = MenuSpinnerAdapter(context, 0, states)
    }
}

data class MenuSpinnerItemState(var title: String? = null, var isSelected: Boolean = false)
data class ViewHolder(var mTextView:TextView, var mCheckBox:CheckBox)


class MenuSpinnerAdapter(private val mContext: Context, resource: Int, private val listState: ArrayList<MenuSpinnerItemState>) : ArrayAdapter<MenuSpinnerItemState>(mContext, resource, listState) {
    private var isFromView = false


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
        val holder: ViewHolder
        if (convertView == null) {
            val layoutInflator = LayoutInflater.from(mContext)
            convertView = layoutInflator.inflate(R.layout.menu_spinner_items, null)
            holder = ViewHolder(
                convertView.findViewById(R.id.menu_item_text),
                convertView.findViewById(R.id.menu_item_checkbox)
            )
            convertView.setTag(holder)
        } else {
            holder = convertView.getTag() as ViewHolder
        }
        val title = when(listState[position].title!!.startsWith("[]")) {
            true -> listState[position].title!!.substring(2)
            false -> listState[position].title!!
        }
        val checkBoxIsVisible = when(listState[position].title!!.startsWith("[]")) {
            true -> true
            false -> false
        }


//        // To check weather checked event fire from getview() or user input
//        isFromView = true
//        holder.mCheckBox.isChecked = listState[position].isSelected
//        isFromView = false

        holder.mTextView.setText(title)
        if (checkBoxIsVisible) {
            holder.mCheckBox.visibility = View.VISIBLE
        } else {
            holder.mCheckBox.visibility = View.INVISIBLE
//            holder.mCheckBox.visibility = View.VISIBLE
//            holder.mCheckBox.isClickable = false
        }
        holder.mCheckBox.tag = position
//        holder.mCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
//            val getPosition = buttonView.tag as Int
//            if (!isFromView) {
//                listState[position].isSelected = isChecked
//            }
//        }
        return convertView!!
    }
}