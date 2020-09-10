package com.example.flectamplifyar.model

import android.util.Log
import com.example.flectamplifyar.AppSettings
import com.example.flectamplifyar.rendering.LineUtils
import com.google.ar.core.Pose
import java.util.*
import javax.vecmath.Vector3f


class Stroke {
    private var points = ArrayList<Vector3f>()

    private var lineWidth = 0f

    var creator = ""

    private var biquadFilter: BiquadFilter? = null

    private val animationFilter: BiquadFilter

    var localLine = true

    var animatedLength = 0f

    var totalLength = 0f

    var finished = false

    // Add point to stroke
    fun add(point: Vector3f) {
        var point = point
        val s = points.size
        if (s == 0) {
            // Prepare the biquad filter
            biquadFilter = BiquadFilter(AppSettings.smoothing.toDouble(), 3)
            for (i in 0 until AppSettings.smoothingCount) {
                biquadFilter!!.update(point)
            }
        }

        // Filter the point
        point = biquadFilter!!.update(point)

        // Check distance, and only add if moved far enough
        if (s > 0) {
            val lastPoint = points[s - 1]
            val temp = Vector3f()
            temp.sub(point, lastPoint)
            if (temp.length() < lineWidth / 10) {
                Log.e("--------", "ADDPOINT NEAR!")
                //return
            }
        }

        // Add the point
        points.add(point)

        // Cleanup vertices that are redundant
        if (s > 3) {
            val angle = calculateAngle(s - 2)
            // Remove points that have very low angle change
            if (angle < 0.05) {
                points.removeAt(s - 2)
                Log.e("--------", "ADDPOINT REMOVE!!")
            } else {
                subdivideSection(s - 3, 0.3f, 0)
                Log.e("--------", "ADDPOINT SUB DIVID!")

            }
        }

        // Cleanup beginning, remove points that are close to each other
        // This makes the end seem straing
//        if(s < 5 && s > 2){
//            float dist = calculateDistance(0, s-1);
//            if(dist < 0.005) {
//                for (int i = 0; i < s - 2; i++) {
//                    if (calculateDistance(i, i + 1) < 0.005) {
//                        points.remove(i + 1);
//                        startCap.clear();
//                    }
//                }
//            }
//        }
        calculateTotalLength()
    }

    /**
     * Update called when there is new data from Firebase
     *
     * @param data Stroke data to copy from
     */
    fun updateStrokeData(data: Stroke) {
        points = data.points
        lineWidth = data.lineWidth
        calculateTotalLength()
    }

    fun update(): Boolean {
        var renderNeedsUpdate = false
        if (!localLine) {
            val before = animatedLength
            animatedLength = animationFilter.update(totalLength)
            if (Math.abs(animatedLength - before) > 0.001) {
                renderNeedsUpdate = true
            }
        }
        return renderNeedsUpdate
    }

    fun finishStroke() {
        finished = true

        // Calculate total distance traveled
        var dist = 0f
        val d = Vector3f()
        for (i in 0 until points.size - 1) {
            d.sub(points[i], points[i + 1])
            dist += d.length()
        }

        // If line is very short, overwrite it
        if (dist < 0.01) {
            if (points.size > 2) {
                val p1 = points[0]
                val p2 = points[points.size - 1]
                points.clear()
                points.add(p1)
                points.add(p2)
            } else if (points.size == 1) {
                val v = Vector3f(points[0])
                v.y += 0.0005f
                points.add(v)
            }
        }
    }

    private fun calculateDistance(index1: Int, index2: Int): Float {
        val p1 = points[index1]
        val p2 = points[index2]
        val n1 = Vector3f()
        n1.sub(p2, p1)
        return n1.length()
    }

    private fun calculateAngle(index: Int): Float {
        val p1 = points[index - 1]
        val p2 = points[index]
        val p3 = points[index + 1]
        val n1 = Vector3f()
        n1.sub(p2, p1)
        val n2 = Vector3f()
        n2.sub(p3, p2)
        return n1.angle(n2)
    }

    fun calculateTotalLength() {
        totalLength = 0f
        for (i in 1 until points.size) {
            val dist = Vector3f(points[i])
            dist.sub(points[i - 1])
            totalLength += dist.length()
        }
    }

    private fun subdivideSection(s: Int, maxAngle: Float, iteration: Int) {
        if (iteration == 6) {
            return
        }
        val p1 = points[s]
        val p2 = points[s + 1]
        val p3 = points[s + 2]
        val n1 = Vector3f()
        n1.sub(p2, p1)
        val n2 = Vector3f()
        n2.sub(p3, p2)
        val angle = n1.angle(n2)

        // If angle is too big, add points
        if (angle > maxAngle) {
            n1.scale(0.5f)
            n2.scale(0.5f)
            n1.add(p1)
            n2.add(p2)
            points.add(s + 1, n1)
            points.add(s + 3, n2)
            subdivideSection(s + 2, maxAngle, iteration + 1)
            subdivideSection(s, maxAngle, iteration + 1)
        }
    }

    fun offsetToPose(pose: Pose) {
        for (i in points.indices) {
            val p: Vector3f = LineUtils.TransformPointToPose(points[i], pose)
            points[i] = p
        }
    }

    fun offsetFromPose(pose: Pose) {
        for (i in points.indices) {
            val p: Vector3f = LineUtils.transformPointFromPose(points[i], pose)
            points[i] = p
        }
    }

    operator fun get(index: Int): Vector3f {
        return points[index]
    }

    fun size(): Int {
        return points.size
    }

    fun getPoints(): List<Vector3f> {
        return points
    }

    fun getLineWidth(): Float {
        return lineWidth
    }

    fun setLineWidth(lineWidth: Float) {
        this.lineWidth = lineWidth
    }

    fun copy(): Stroke {
        val copy = Stroke()
        copy.creator = creator
        copy.lineWidth = lineWidth
        copy.points = ArrayList(points)
        return copy
    }

    companion object {
        private const val TAG = "Stroke"
    }

    init {
        // Default constructor required for calls to DataSnapshot.getValue(Stroke.class)
        animationFilter = BiquadFilter(0.025, 1)
    }
}
