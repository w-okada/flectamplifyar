package com.example.flectamplifyar.helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object CameraPermissionHelper{
    const private val CAMERA_PERMISSION_CODE = 0
    const private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private const val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    private val REQUIRED_PERMISSIONS = arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSION)


    fun hasCameraPermission(activity: Activity): Boolean {
        var hasNeededPermissions = true
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission).equals(PackageManager.PERMISSION_DENIED)) {
                hasNeededPermissions = false
            }
        }
        return hasNeededPermissions
    }

    fun requestCameraPermission(activity: Activity) {
//        ActivityCompat.requestPermissions(activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
        ActivityCompat.requestPermissions(activity,  REQUIRED_PERMISSIONS, CAMERA_PERMISSION_CODE)
    }

    fun shouldShowRequestPermissionRationale(activity: Activity?): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity!!, CAMERA_PERMISSION
        )
    }

    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

}