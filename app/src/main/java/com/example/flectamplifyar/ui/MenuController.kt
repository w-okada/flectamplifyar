package com.example.flectamplifyar.ui


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment_menu.*
import kotlinx.android.synthetic.main.arfragment_menu.view.*
import kotlinx.android.synthetic.main.image_capture_view.*
import kotlinx.android.synthetic.main.image_capture_view.view.*


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

        loadMarkerButton.setOnClickListener{
            findNavController().navigate(R.id.action_first_to_second)
        }

        captureMarkerButton.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                showMenuButton.text = context.getString(R.string.menu_button_text_enter)
                menuView.visibility = View.INVISIBLE
                rootView.imageCaptureView.visibility = View.VISIBLE

            }
        })

        sighOutButton.setOnClickListener{
            Amplify.Auth.signOut(
                { Log.i("AuthQuickstart", "Signed out successfully") },
                { error -> Log.e("AuthQuickstart", error.toString()) }
            )
            (context as AppCompatActivity).finish()
            (context as AppCompatActivity).moveTaskToBack(true);
        }




    }
}