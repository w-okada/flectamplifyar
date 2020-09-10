package com.example.flectamplifyar.helper

import android.util.Log
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Session
import java.util.*
import java.util.Collections.sort

object CameraConfigHelper{
    enum class ImageResolution {
        LOW_RESOLUTION, MEDIUM_RESOLUTION, HIGH_RESOLUTION
    }
    lateinit private var cpuLowResolutionCameraConfig: CameraConfig
    lateinit private var cpuMediumResolutionCameraConfig: CameraConfig
    lateinit private var cpuHighResolutionCameraConfig: CameraConfig
    var cpuResolution = ImageResolution.HIGH_RESOLUTION

    fun setup(session: Session?) {
        // First obtain the session handle before getting the list of various camera configs.
        if (session != null) {
            // Create filter here with desired fps filters.
            val cameraConfigFilter = CameraConfigFilter(session).setTargetFps(
                EnumSet.of(
                    CameraConfig.TargetFps.TARGET_FPS_30, CameraConfig.TargetFps.TARGET_FPS_60))
            val cameraConfigs = session.getSupportedCameraConfigs(cameraConfigFilter)
            Log.i("TAG","Size of supported CameraConfigs list is " + cameraConfigs.size)

            // Determine the highest and lowest CPU resolutions.
            cpuLowResolutionCameraConfig =
                getCameraConfigWithSelectedResolution(cameraConfigs,  ImageResolution.LOW_RESOLUTION)
            cpuMediumResolutionCameraConfig =
                getCameraConfigWithSelectedResolution(cameraConfigs,  ImageResolution.MEDIUM_RESOLUTION)
            cpuHighResolutionCameraConfig =
                getCameraConfigWithSelectedResolution(cameraConfigs,  ImageResolution.HIGH_RESOLUTION)

            cpuResolution = ImageResolution.HIGH_RESOLUTION
        }
    }

    fun getCurrentCameraConfig(): CameraConfig{
        return when (cpuResolution) {
            ImageResolution.LOW_RESOLUTION    -> cpuLowResolutionCameraConfig
            ImageResolution.MEDIUM_RESOLUTION -> cpuMediumResolutionCameraConfig
            ImageResolution.HIGH_RESOLUTION   -> cpuHighResolutionCameraConfig
        }
    }


    private fun getCameraConfigWithSelectedResolution(cameraConfigs: List<CameraConfig>, resolution: ImageResolution): CameraConfig {
        val cameraConfigsByResolution: List<CameraConfig> = ArrayList(cameraConfigs.subList(0, Math.min(cameraConfigs.size, 3)))
        sort(cameraConfigsByResolution) { p1: CameraConfig, p2: CameraConfig ->
            Integer.compare(p1.imageSize.height, p2.imageSize.height)
        }
        val cameraConfig = when (resolution) {
            ImageResolution.LOW_RESOLUTION    -> cameraConfigsByResolution[0]
            ImageResolution.MEDIUM_RESOLUTION -> if(cameraConfigsByResolution.size>=2) cameraConfigsByResolution[1] else cameraConfigsByResolution[0]
            ImageResolution.HIGH_RESOLUTION   -> if(cameraConfigsByResolution.size>=3) cameraConfigsByResolution[2] else cameraConfigsByResolution[0]
        }
        return cameraConfig
    }

}