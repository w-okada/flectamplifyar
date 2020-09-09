package com.example.flectamplifyar

import javax.vecmath.Vector3f


object AppSettings {
    val color = Vector3f(1f, 1f, 1f)
    const val strokeDrawDistance = 0.13f
    const val minDistance = 0.000001f
    const val nearClip = 0.001f
    const val farClip = 100.0f
    const val smoothing = 0.07f
    const val smoothingCount = 1500

    enum class LineWidth(val width: Float) {
        SMALL(0.006f), MEDIUM(0.011f), LARGE(0.020f);

    }
}
