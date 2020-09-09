package com.example.flectamplifyar.helper

import android.content.Context
import android.content.SharedPreferences

object DepthSettings{
    const private val SHARED_PREFERENCES_ID = "SHARED_PREFERENCES_OCCLUSION_OPTIONS"
    const private val SHARED_PREFERENCES_SHOW_DEPTH_ENABLE_DIALOG_OOBE = "show_depth_enable_dialog_oobe"
    const private val SHARED_PREFERENCES_USE_DEPTH_FOR_OCCLUSION = "use_depth_for_occlusion"

    private var depthColorVisualizationEnabled = false
    private var useDepthForOcclusion = false
    lateinit private var sharedPreferences: SharedPreferences


    fun onCreate(context: Context) {
        sharedPreferences    = context.getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE)
        useDepthForOcclusion = sharedPreferences.getBoolean(SHARED_PREFERENCES_USE_DEPTH_FOR_OCCLUSION, false)
    }

    fun useDepthForOcclusion(): Boolean {
        return useDepthForOcclusion
    }

    fun setUseDepthForOcclusion(enable: Boolean) {
        if (enable == useDepthForOcclusion) {
            return  // No change.
        }

        // Updates the stored default settings.
        useDepthForOcclusion = enable
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            SHARED_PREFERENCES_USE_DEPTH_FOR_OCCLUSION,
            useDepthForOcclusion
        )
        editor.apply()
    }

    fun depthColorVisualizationEnabled(): Boolean {
        return depthColorVisualizationEnabled
    }

    fun setDepthColorVisualizationEnabled(depthColorVisualizationEnabled: Boolean) {
        this.depthColorVisualizationEnabled = depthColorVisualizationEnabled
    }

    fun shouldShowDepthEnableDialog(): Boolean {
        // Checks if this dialog has been called before on this device.
        val showDialog = sharedPreferences.getBoolean(SHARED_PREFERENCES_SHOW_DEPTH_ENABLE_DIALOG_OOBE, true)
        if (showDialog) {
            // Only ever shows the dialog on the first time.  If the user wants to adjust these settings
            // again, they can use the gear icon to invoke the settings menu dialog.
            val editor = sharedPreferences.edit()
            editor.putBoolean(SHARED_PREFERENCES_SHOW_DEPTH_ENABLE_DIALOG_OOBE, false)
            editor.apply()
        }
        return showDialog
    }

}