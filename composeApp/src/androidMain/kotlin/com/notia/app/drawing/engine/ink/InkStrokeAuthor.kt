package com.notia.app.drawing.engine.ink

import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.ink.authoring.InProgressStrokeId
import androidx.ink.authoring.InProgressStrokesFinishedListener
import androidx.ink.authoring.InProgressStrokesView
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.Stroke
import androidx.input.motionprediction.MotionEventPredictor

/**
 * Handles all input logic: capturing touch events, prediction, and creating Ink strokes.
 * Encapsulates InProgressStrokesView.
 */
class InkStrokeAuthor(context: Context) : FrameLayout(context) {

    private val inputView = InProgressStrokesView(context)
    private var predictor: MotionEventPredictor? = null
    
    // Listener for when a stroke is fully formed
    private var onStrokeCommitted: ((Stroke) -> Unit)? = null

    // Debug Listener
    private var onDebugStatsUpdated: ((String, List<DebugPoint>) -> Unit)? = null
    
    // Debug Data Class
    data class DebugPoint(
        val x: Float, 
        val y: Float, 
        val velocity: Float, // px/sec
        val type: PointType = PointType.RAW
    )
    
    enum class PointType { RAW, ACCEPTED, REJECTED }

    // Debug State
    private val debugPoints = mutableListOf<DebugPoint>()
    private var lastEventTime = 0L
    private var eventCount = 0
    private var startTime = 0L

    // Filter Logic
    private var lastAcceptedX = 0f
    private var lastAcceptedY = 0f
    private var lastAcceptedTime = 0L
    private var lastAcceptedPressure = 0f
    
    // For Angle Calculation (we need prevAccepted too)
    private var prevAcceptedX = 0f
    private var prevAcceptedY = 0f
    
    // Configurable Parameters
    private val minDistanceThresholdDp = 0.8f
    private var minDistanceThresholdPx = 0f // Calculated in init
    private val minTimeThresholdMs = 6L
    
    // Stats for Debugging
    private var rawPointCount = 0
    private var acceptedPointCount = 0
    private var totalAcceptedDistance = 0f
    
    // State
    private var currentBrush: Brush = createDefaultBrush()
    private var currentStrokeId: InProgressStrokeId? = null

    init {
        // Calculate px threshold
        val density = context.resources.displayMetrics.density
        minDistanceThresholdPx = minDistanceThresholdDp * density
        
        addView(inputView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        
        // Initialize predictor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            predictor = MotionEventPredictor.newInstance(this)
        }

        // Setup Listener
        inputView.addFinishedStrokesListener(object : InProgressStrokesFinishedListener {
            override fun onStrokesFinished(strokes: Map<InProgressStrokeId, Stroke>) {
                strokes.values.forEach { stroke ->
                    onStrokeCommitted?.invoke(stroke)
                }
            }
        })
    }
    
    fun setOnStrokeCommittedListener(listener: (Stroke) -> Unit) {
        onStrokeCommitted = listener
    }

    fun setDebugListener(listener: (String, List<DebugPoint>) -> Unit) {
        onDebugStatsUpdated = listener
    }
    
    fun updateTool(brush: Brush, isEraser: Boolean = false) {
        if (isEraser) {
            // Eraser logic will be handled by DrawingEngine/Canvas setting state, 
            // but if Author needs to know, we can store it.
            // For Ink API 1.0, Eraser is often just "not adding strokes" or using a specific eraser tool.
            // However, typical implementation separates Eraser Mode.
            // Let's rely on the Brush for now, but really Eraser usually requires a different INPUT mode in some SDKs.
            // For androidx.ink, we might just be setting a "transparent" brush or handling it in the engine.
            // Actually, `InkRenderer` doesn't support "erasing" by drawing transparently easily.
            // Usually we do geometry-based erasing on the strokes list.
            // So for "Author", we just stop "creating" strokes if we want to handle eraser purely in UI/VM,
            // OR we create a "Eraser Stroke" that the VM uses to delete intersecting strokes.
            // Let's assume we create a stroke and let VM handle intersection/deletion for now, 
            // OR we just assume the Engine handles eraser mode.
            currentBrush = brush
        } else {
            currentBrush = brush
        }
    }
    
    // Yardımcı metot
    fun createBrush(colorInt: Int, size: Float, family: String): Brush {
        // StockBrushes.pressurePenV1 or StockBrushes.highlighterV1
        val brushFamily = if (family == "highlighter") StockBrushes.highlighterV1 else StockBrushes.pressurePenV1
        return Brush.createWithColorIntArgb(
            family = brushFamily,
            colorIntArgb = colorInt,
            size = size,
            epsilon = 0.01f
        )
    }

    /**
     * Main entry point for touch events. 
     * Handles Unbuffered Dispatch and Prediction.
     */
    fun handleTouchEvent(event: MotionEvent): Boolean {
        // 1. Unbuffered Dispatch
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            this.requestUnbufferedDispatch(event)
        }

        // 2. Prediction Record
        predictor?.record(event)

        // 3. Multi-touch Rejection Logic
        // If more than 1 pointer is active, we assume it's a zoom/pan gesture, NOT drawing.
        if (event.pointerCount > 1) {
            // Check if we were drawing, if so, CANCEL it.
            if (currentStrokeId != null) {
                cancelCurrentStroke(event)
            }
            // Allow parent to intercept (Zoom/Pan)
            parent?.requestDisallowInterceptTouchEvent(false)
            return false 
        }

        val pointerId = event.getPointerId(0)
        
        // Debug Data Collection
        val x = event.x
        val y = event.y
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            debugPoints.clear()
            eventCount = 0
            startTime = System.currentTimeMillis()
        }
        if (event.actionMasked == MotionEvent.ACTION_MOVE || 
            event.actionMasked == MotionEvent.ACTION_DOWN || 
            event.actionMasked == MotionEvent.ACTION_UP) { // Capture UP event too
             // Calculate Velocity (Instantaneous) for Debug
             val dtRaw = if (lastEventTime > 0) (System.currentTimeMillis() - lastEventTime) else 0L
             // Note: event.eventTime is better but let's use system diff for debug rate consistency
             
             // We need to keep track of last raw point for velocity calc
             // But simpler: just use eventCount / elapsed for global rate
             
             // Better velocity: use the filter logic's computed velocity when available, 
             // but for RAW points we might just default to 0 or calculate relative to previous raw.
             val currentV = 0f // Raw points velocity is noisy calculate later if needed
             
             debugPoints.add(DebugPoint(x, y, 0f, PointType.RAW))
             
             // Update logic (only count moves/downs for rate to avoid skewers)
             if (event.actionMasked != MotionEvent.ACTION_UP) {
                 eventCount++
                 val now = System.currentTimeMillis()
                 if (now - lastEventTime > 200) { 
                     val elapsed = (now - startTime) / 1000f
                     val rate = if (elapsed > 0) eventCount / elapsed else 0f
                     
                     // Calculate Avg Spacing
                     val avgSpacing = if (acceptedPointCount > 1) {
                         val density = context.resources.displayMetrics.density
                         (totalAcceptedDistance / (acceptedPointCount - 1)) / density // in dp
                     } else 0f
                     
                     val stats = "Rate: ${rate.toInt()} Hz | Raw: ${debugPoints.size} | Acc: $acceptedPointCount | Rej: ${debugPoints.size - acceptedPointCount} | Avg: %.2f dp".format(avgSpacing)
                     
                     onDebugStatsUpdated?.invoke(stats, debugPoints.toList())
                     lastEventTime = now
                 }
             } else {
                 // Final update on UP
                 val avgSpacing = if (acceptedPointCount > 1) {
                     val density = context.resources.displayMetrics.density
                     (totalAcceptedDistance / (acceptedPointCount - 1)) / density // in dp
                 } else 0f
                 
                 val stats = "Final | Raw: ${debugPoints.size} | Acc: $acceptedPointCount | Rej: ${debugPoints.size - acceptedPointCount} | Avg: %.2f dp".format(avgSpacing)
                 onDebugStatsUpdated?.invoke(stats, debugPoints.toList())
             } // End Debug Stats Block
        }

        when (event.actionMasked) {
             MotionEvent.ACTION_DOWN -> {
                // CRITICAL FIX: Prevent parent from intercepting while we are single-finger drawing
                parent?.requestDisallowInterceptTouchEvent(true)
                
                try {
                    currentStrokeId = inputView.startStroke(
                        event, 
                        pointerId, 
                        currentBrush, 
                        Matrix(), 
                        Matrix()
                    )
                    // Reset filter state
                    lastAcceptedX = event.x
                    lastAcceptedY = event.y
                    lastAcceptedTime = event.eventTime
                    lastAcceptedPressure = event.pressure
                    prevAcceptedX = event.x
                    prevAcceptedY = event.y
                    
                    // Reset Stats
                    rawPointCount = 1 // DOWN counts as 1
                    acceptedPointCount = 1
                    totalAcceptedDistance = 0f
                } catch (e: Exception) {
                    currentStrokeId = null
                }
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                 // Second finger touched -> Cancel drawing immediately & release interception
                 cancelCurrentStroke(event)
                 parent?.requestDisallowInterceptTouchEvent(false)
                 return false
            }
            MotionEvent.ACTION_MOVE -> {
                val id = currentStrokeId ?: return false
                
                // Prediction Playback (Unchanged - Prediction needs unfiltered stream usually, but let's test)
                // Actually, predictor.record() handles the raw stream above.
                     // Prediction removed to enforce "Accepted Point Invariant"
                     // We process raw events strictly through the filter.
                     
                     try {
                         // INPUT FILTER LOGIC
                         val currentX = event.x
                         val currentY = event.y
                         val currentTime = event.eventTime
                         val currentPressure = event.pressure
    
                         val dt = currentTime - lastAcceptedTime
                         
                         val dx = currentX - lastAcceptedX
                         val dy = currentY - lastAcceptedY
                         val dist = kotlin.math.hypot(dx, dy)
                         
                         var accept = false
                         
                         // 1. Standard Distance Threshold (1.2dp)
                     // Task 3: REMOVED time-based acceptance here to prevent oversampling on slow strokes.
                     // Distance is now the primary driver.
                     // Task 5 Tuning: Adjusted to 1.0dp (Sweet spot) per user feedback.
                     val minDistanceDp = 1.0f
                     val minDistancePx = minDistanceDp * context.resources.displayMetrics.density

                     if (dist > minDistancePx) {
                         accept = true
                     }
                     
                     // 2. Corner Preservation
                     // Task 3: Gate with time check
                     if (!accept && acceptedPointCount > 1 && dt > minTimeThresholdMs) {
                         val v1x = lastAcceptedX - prevAcceptedX
                         val v1y = lastAcceptedY - prevAcceptedY
                         val v2x = dx
                         val v2y = dy
                         
                         val dot = v1x * v2x + v1y * v2y
                         val mag1 = kotlin.math.hypot(v1x, v1y)
                         val mag2 = dist
                         
                         if (mag1 > 0 && mag2 > 0) {
                             val cosTheta = dot / (mag1 * mag2)
                             val clampedCos = cosTheta.coerceIn(-1.0f, 1.0f)
                             val angleRad = kotlin.math.acos(clampedCos)
                             val angleDeg = Math.toDegrees(angleRad.toDouble())
                             
                             // Task 6 Algorithm: Dynamic Angle Threshold
                             // Low Velocity (< 80 dp/s) -> 45 degrees (Filter tremors in slow writing)
                             // Normal/Fast (> 80 dp/s) -> 30 degrees (Preserve sharpness)
                             val candidateDistDp = dist / context.resources.displayMetrics.density
                             val velocityDpSec = if (dt > 0) (candidateDistDp / dt) * 1000f else 0f
                             
                             val angleThreshold = if (velocityDpSec < 80f) 45.0 else 30.0
                             
                             if (kotlin.math.abs(angleDeg) > angleThreshold) {
                                 accept = true
                             }
                         }
                     }
                     
                     // 3. Pressure Preservation
                     // Task 3: Gate with time check
                     if (!accept && dt > minTimeThresholdMs) {
                         if (kotlin.math.abs(currentPressure - lastAcceptedPressure) > 0.03f) {
                             accept = true
                         }
                     }
    
                         if (accept) {
                             inputView.addToStroke(event, pointerId, id)
                             
                             val velocityPxSec = if (dt > 0) (dist / dt) * 1000f else 0f
                             
                             totalAcceptedDistance += dist.toFloat()
                             acceptedPointCount++
                             
                             debugPoints.add(DebugPoint(currentX, currentY, velocityPxSec, PointType.ACCEPTED))
    
                             prevAcceptedX = lastAcceptedX
                             prevAcceptedY = lastAcceptedY
                             
                             lastAcceptedX = currentX
                             lastAcceptedY = currentY
                             lastAcceptedTime = currentTime
                             lastAcceptedPressure = currentPressure
                         }
                    } catch (e: Exception) {
                        // Ignore
                    }
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Allow interception again after stroke is finished
                parent?.requestDisallowInterceptTouchEvent(false)
                
                val id = currentStrokeId ?: return false
                try {
                    inputView.addToStroke(event, pointerId, id)
                    inputView.finishStroke(event, pointerId, id)
                } catch (e: Exception) {
                    // Log error but don't crash
                } finally {
                    currentStrokeId = null
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                 cancelCurrentStroke(event)
                 return true
            }
        }
        return false
    }

    private fun cancelCurrentStroke(event: MotionEvent) {
        // Allow interception again
        parent?.requestDisallowInterceptTouchEvent(false)
         
        val id = currentStrokeId ?: return
        inputView.cancelStroke(id, event)
        currentStrokeId = null
    }

    private fun createDefaultBrush(): Brush {
        return Brush.createWithColorIntArgb(
            family = StockBrushes.pressurePenV1,
            colorIntArgb = android.graphics.Color.BLACK,
            size = 5f,
            epsilon = 0.01f
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true // Capture all
    }
}
