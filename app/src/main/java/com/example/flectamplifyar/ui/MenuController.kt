package com.example.flectamplifyar.ui


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.amplifyframework.core.Amplify
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.arfragment_menu.view.*
import kotlinx.android.synthetic.main.image_capture_view.*
import kotlinx.android.synthetic.main.load_marker_view.*


class MenuController: ConstraintLayout {
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.arfragment_menu, this, true)
    }

    lateinit var arFragment:ARFragment

    enum class operation{
        toShow, toHide
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)

        // Menu/Exitボタンが押されたときの処理
        showMenuButton.setOnClickListener{
            // Menu処理/Exit処理の切り分け
            val Operation = when(menuView.visibility){
                View.VISIBLE -> operation.toHide
                View.INVISIBLE -> operation.toShow
                else -> operation.toHide
            }

            //// Menu/Exitボタンの切り替え
            showMenuButton.text = when(Operation){
                operation.toHide -> context.getString(R.string.menu_button_text_enter)
                operation.toShow -> context.getString(R.string.menu_button_text_exit)
            }

            //// メニューダイアログの表示/非表示切り替え
            menuView.visibility = when(Operation){
                operation.toHide -> View.INVISIBLE
                operation.toShow -> View.VISIBLE
            }
        }

        // LoadMarkerボタンがクリックされあたときの処理
        loadMarkerButton.setOnClickListener{
            arFragment.loadMarkerView2.generateListView() // ここでmarker listを生成するのはいけてない。visibilityの変化をトリガにしたいところ
            showMenuButton.text = context.getString(R.string.menu_button_text_enter)
            menuView.visibility = View.INVISIBLE
            arFragment.loadMarkerView.visibility = View.VISIBLE // OK
        }

        // Captureボタンがクリックされたときの処理
        captureMarkerButton.setOnClickListener{
            showMenuButton.text = context.getString(R.string.menu_button_text_enter)
            menuView.visibility = View.INVISIBLE
            arFragment.imageCaptureView.visibility = View.VISIBLE // OK
            //imageCaptureView.visibility = View.VISIBLE // NG Nullpointer
            //rootView.imageCaptureView.visibility = View.VISIBLE // OK
        }


        // サインアウトボタンがクリックされたときの処理
        sighOutButton.setOnClickListener{
            Amplify.Auth.signOut(
                { Log.i("AuthQuickstart", "Signed out successfully") },
                { error -> Log.e("AuthQuickstart", error.toString()) }
            )
            showMenuButton.text = "MENU"
            menuView.visibility = View.INVISIBLE
        }
    }
}