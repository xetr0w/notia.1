package com.notia.app.features.drawing.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.notia.app.features.drawing.model.Point
import com.notia.app.features.drawing.model.Stroke
import kotlin.math.abs

class DrawingEngine {

    // Threshold to ignore micro-movements (jitter reduction)
    private val touchTolerance = 2f

    fun createPath(points: List<Point>): Path {
        val path = Path()
        if (points.isEmpty()) return path

        // Start at the first point
        var currentPoint = points.first()
        path.moveTo(currentPoint.x, currentPoint.y)

        if (points.size < 2) {
            // Single point (dot)
            path.addOval(androidx.compose.ui.geometry.Rect(
                center = Offset(currentPoint.x, currentPoint.y),
                radius = 0.5f
            ))
            return path
        }

        // Quadratic Bezier Smoothing
        // Connect points using control points for smooth curves
        for (i in 1 until points.size) {
            val nextPoint = points[i]
            
            // Midpoint approach for smoothness
            // Curve from current to midpoint of (current, next)
            // But standard quadratic is: quadTo(x1, y1, x2, y2)
            // x1,y1 is control, x2,y2 is end.
            
            // To make it pass THROUGH points or near them smoothly, 
            // a common alg uses midpoints as the start/end of curves.
            
            val xm = (currentPoint.x + nextPoint.x) / 2
            val ym = (currentPoint.y + nextPoint.y) / 2
            
            path.quadraticBezierTo(currentPoint.x, currentPoint.y, xm, ym)
            
            currentPoint = nextPoint
        }

        // Connect to the absolute last point
        path.lineTo(currentPoint.x, currentPoint.y)
        
        return path
    }

    // Simplistic input smoother for real-time construction
    // Returns true if the point is significant enough to add
    fun shouldAddPoint(lastPoint: Point?, newX: Float, newY: Float): Boolean {
        if (lastPoint == null) return true
        val dx = abs(newX - lastPoint.x)
        val dy = abs(newY - lastPoint.y)
        return dx >= touchTolerance || dy >= touchTolerance
    }
}
