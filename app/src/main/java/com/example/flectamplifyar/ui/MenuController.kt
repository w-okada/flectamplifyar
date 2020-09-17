package com.example.flectamplifyar.ui


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment_menu.view.*


class MenuController: ConstraintLayout {
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.arfragment_menu, this, true)
    }

    enum class operation{
        toShow, toHide
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        showMenuButton.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                val Operation = when(menuView.visibility){
                    View.VISIBLE -> operation.toHide
                    View.INVISIBLE -> operation.toShow
                    else -> operation.toHide
                }

                showMenuButton.text = when(Operation){
                    operation.toHide -> context.getString(R.string.menu_button_text_enter)
                    operation.toShow -> context.getString(R.string.menu_button_text_exit)
                }

                menuView.visibility = when(Operation){
                    operation.toHide -> View.INVISIBLE
                    operation.toShow -> View.VISIBLE
                }
            }
        })
    }
}