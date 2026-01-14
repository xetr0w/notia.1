package com.notia.app.features.drawing.logic

import com.notia.app.features.drawing.model.Point
import kotlin.math.hypot

class InputSampler(
    private val minDistancePx: Float = 2.0f,
    private val minTimeMs: Long = 10L
) {
    private var lastPoint: Point? = null

    fun shouldAccept(x: Float, y: Float, timestamp: Long): Boolean {
        if (lastPoint == null) {
            lastPoint = Point(x, y, 1.0f, timestamp)
            return true
        }

        val lp = lastPoint!!
        val dist = hypot(x - lp.x, y - lp.y)
        val timeDelta = timestamp - lp.timestamp

        // Accept if moved enough OR waited enough
        if (dist >= minDistancePx || timeDelta >= minTimeMs) {
            lastPoint = Point(x, y, 1.0f, timestamp)
            return true
        }
        return false
    }

    fun reset() {
        lastPoint = null
    }
}
