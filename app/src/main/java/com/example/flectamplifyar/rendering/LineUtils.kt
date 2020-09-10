package com.example.flectamplifyar.rendering

import android.opengl.Matrix
import com.example.flectamplifyar.AppSettings
import com.example.flectamplifyar.model.Ray
import com.google.ar.core.Pose
import javax.vecmath.Vector2f
import javax.vecmath.Vector3f


object LineUtils {
    fun getWorldCoords(touchPoint: Vector2f?, screenWidth: Float, screenHeight: Float, projectionMatrix: FloatArray, viewMatrix: FloatArray): Vector3f {
        val touchRay: Ray = projectRay(Vector2f(touchPoint), screenWidth, screenHeight, projectionMatrix, viewMatrix)
        touchRay.direction.scale(AppSettings.strokeDrawDistance)
        touchRay.origin.add(touchRay.direction)
        return touchRay.origin
    }

    private fun screenPointToRay(point: Vector2f, viewportSize: Vector2f, viewProjMtx: FloatArray): Ray {
        point.y = viewportSize.y - point.y // 原点を左上から左下に変換
        val x = point.x * 2.0f / viewportSize.x - 1.0f // 原点をスクリーンの中心に、-1 ~ 1の座標にする
        val y = point.y * 2.0f / viewportSize.y - 1.0f // 原点をスクリーンの中心にして、-1 ~ 1の座標にする
        val farScreenPoint = floatArrayOf(x, y, 1.0f, 1.0f)
        val nearScreenPoint = floatArrayOf(x, y, -1.0f, 1.0f)
        val nearPlanePoint = FloatArray(4)
        val farPlanePoint = FloatArray(4)
        val invertedProjectionMatrix = FloatArray(16)
        Matrix.setIdentityM(invertedProjectionMatrix, 0)
        Matrix.invertM(invertedProjectionMatrix, 0, viewProjMtx, 0)
        Matrix.multiplyMV(nearPlanePoint, 0, invertedProjectionMatrix, 0, nearScreenPoint, 0)
        Matrix.multiplyMV(farPlanePoint, 0, invertedProjectionMatrix, 0, farScreenPoint, 0)
        val direction = Vector3f(farPlanePoint[0] / farPlanePoint[3], farPlanePoint[1] / farPlanePoint[3], farPlanePoint[2] / farPlanePoint[3])
        val origin = Vector3f(
            Vector3f(nearPlanePoint[0] / nearPlanePoint[3], nearPlanePoint[1] / nearPlanePoint[3], nearPlanePoint[2] / nearPlanePoint[3])
        )
        direction.sub(origin)
        direction.normalize()
        return Ray(origin, direction)
    }

    private fun projectRay(touchPoint: Vector2f, screenWidth: Float, screenHeight: Float, projectionMatrix: FloatArray, viewMatrix: FloatArray): Ray {
        val viewProjMtx = FloatArray(16)
        Matrix.multiplyMM(viewProjMtx, 0, projectionMatrix, 0, viewMatrix, 0)
        return screenPointToRay(touchPoint, Vector2f(screenWidth, screenHeight), viewProjMtx)
    }

    fun distanceCheck(newPoint: Vector3f?, lastPoint: Vector3f?): Boolean {
        val temp = Vector3f()
        temp.sub(newPoint, lastPoint)
        return temp.lengthSquared() > AppSettings.minDistance
    }

    /**
     * Transform a vector3f FROM anchor coordinates TO world coordinates
     */
    fun transformPointFromPose(point: Vector3f, anchorPose: Pose): Vector3f {
        var position = FloatArray(3)
        position[0] = point.x
        position[1] = point.y
        position[2] = point.z
        position = anchorPose.transformPoint(position)
        return Vector3f(position[0], position[1], position[2])
    }

    /**
     * Transform a vector3f TO anchor coordinates FROM world coordinates
     */
    fun transformPointToPose(point: Vector3f, anchorPose: Pose): Vector3f {
        // Recenter to anchor
        var position = FloatArray(3)
        position[0] = point.x
        position[1] = point.y
        position[2] = point.z
        position = anchorPose.inverse().transformPoint(position)
        return Vector3f(position[0], position[1], position[2])
    }
}
