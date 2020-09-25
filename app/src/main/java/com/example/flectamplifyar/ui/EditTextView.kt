package com.example.flectamplifyar.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flectamplifyar.R
import com.example.flectamplifyar.hideKeyboard
import kotlinx.android.synthetic.main.edit_text_view.view.*

class EditTextView: ConstraintLayout {
    companion object{
        val TAG = EditTextView::class.java.simpleName
    }
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.edit_text_view, this, true)
    }

    lateinit var arFragment:ARFragment

    override fun onViewAdded(view: View?) {
        edit_text_ok_button.setOnClickListener {
            arFragment.textElementText = edit_texit_field.toString()
            edit_text_view.visibility = View.INVISIBLE
            hideKeyboard()
        }

        edit_text_cancel_button.setOnClickListener {
            edit_text_view.visibility = View.INVISIBLE
            hideKeyboard()
        }

    }

}