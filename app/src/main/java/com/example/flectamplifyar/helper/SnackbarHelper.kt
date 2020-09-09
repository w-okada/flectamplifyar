package com.example.flectamplifyar.helper

import android.R
import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

object SnackbarHelper{
    const private val BACKGROUND_COLOR = -0x40cdcdce

    private var messageSnackbar: Snackbar? = null

    private enum class DismissBehavior { HIDE, SHOW, FINISH }
    private var maxLines = 2
    private var lastMessage = ""
    private var snackbarView: View? = null

    fun isShowing(): Boolean {
        return messageSnackbar != null
    }

    fun showMessage(activity: Activity, message: String) {
        if (!message.isEmpty() && (!isShowing() || lastMessage != message)) {
            lastMessage = message
            show(activity, message, DismissBehavior.HIDE)
        }
    }

    fun showMessageWithDismiss(activity: Activity, message: String) {
        show(activity, message, DismissBehavior.SHOW)
    }

    fun showError(activity: Activity, errorMessage: String) {
        show(activity, errorMessage, DismissBehavior.FINISH)
    }

    fun hide(activity: Activity) {
        if (!isShowing()) {
            return
        }
        lastMessage = ""
        val messageSnackbarToHide: Snackbar = messageSnackbar!!
        messageSnackbar = null
        activity.runOnUiThread { messageSnackbarToHide.dismiss() }
    }

    fun setMaxLines(lines: Int) {
        maxLines = lines
    }

    fun setParentView(snackbarView: View?) {
        this.snackbarView = snackbarView
    }


    private fun show(activity: Activity, message: String, dismissBehavior: DismissBehavior) {
        activity.runOnUiThread {
            messageSnackbar = Snackbar.make(
                (if (snackbarView == null) activity.findViewById(R.id.content) else snackbarView!!),
                message,
                Snackbar.LENGTH_INDEFINITE
            )
            messageSnackbar!!.view.setBackgroundColor(BACKGROUND_COLOR)
            if (dismissBehavior != DismissBehavior.HIDE) {
                messageSnackbar!!.setAction("Dismiss") { messageSnackbar!!.dismiss() }
                if (dismissBehavior == DismissBehavior.FINISH) {
                    messageSnackbar!!.addCallback(
                        object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                activity.finish()
                            }
                        })
                }
            }
            (messageSnackbar!!.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView).maxLines = maxLines
            messageSnackbar!!.show()
        }
    }
}