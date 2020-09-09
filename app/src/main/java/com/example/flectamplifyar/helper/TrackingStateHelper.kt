package com.example.flectamplifyar.helper

import android.app.Activity
import android.view.WindowManager
import com.google.ar.core.Camera
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState

object TrackingStateHelper{
    const private val INSUFFICIENT_FEATURES_MESSAGE = "Can't find anything. Aim device at a surface with more texture or color."
    const private val EXCESSIVE_MOTION_MESSAGE = "Moving too fast. Slow down."
    const private val INSUFFICIENT_LIGHT_MESSAGE = "Too dark. Try moving to a well-lit area."
    const private val BAD_STATE_MESSAGE = "Tracking lost due to bad internal state. Please try restarting the AR experience."
    const private val CAMERA_UNAVAILABLE_MESSAGE = "Another app is using the camera. Tap on this app or try closing the other one."

    lateinit private var activity: Activity
    private var previousTrackingState: TrackingState? = null

    fun setup(activity: Activity){
        this.activity = activity
    }


    fun getTrackingFailureReasonString(camera: Camera): String {
        val reason = camera.trackingFailureReason
        return when (reason) {
            TrackingFailureReason.NONE -> ""
            TrackingFailureReason.BAD_STATE -> BAD_STATE_MESSAGE
            TrackingFailureReason.INSUFFICIENT_LIGHT -> INSUFFICIENT_LIGHT_MESSAGE
            TrackingFailureReason.EXCESSIVE_MOTION -> EXCESSIVE_MOTION_MESSAGE
            TrackingFailureReason.INSUFFICIENT_FEATURES -> INSUFFICIENT_FEATURES_MESSAGE
            TrackingFailureReason.CAMERA_UNAVAILABLE -> CAMERA_UNAVAILABLE_MESSAGE
            else -> "Unknown tracking failure reason: $reason"
        }
    }


    fun updateKeepScreenOnFlag(trackingState: TrackingState) {
        if (trackingState == previousTrackingState) {
            return
        }
        previousTrackingState = trackingState
        when (trackingState) {
            TrackingState.PAUSED, TrackingState.STOPPED -> activity.runOnUiThread {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            TrackingState.TRACKING -> activity.runOnUiThread {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }


}