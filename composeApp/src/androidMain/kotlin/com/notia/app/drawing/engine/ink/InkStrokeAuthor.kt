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
    
    // State
    private var currentBrush: Brush = createDefaultBrush()
    private var currentStrokeId: InProgressStrokeId? = null

    init {
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
    
    fun updateBrush(brush: Brush) {
        currentBrush = brush
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

        // 3. Ink Logic
        if (event.actionIndex != 0) return false // Single pointer for now (Palm rejection effectively via ignore)
        
        val pointerId = event.getPointerId(0)

        when (event.actionMasked) {
             MotionEvent.ACTION_DOWN -> {
                try {
                    currentStrokeId = inputView.startStroke(
                        event, 
                        pointerId, 
                        currentBrush, 
                        Matrix(), 
                        Matrix()
                    )
                } catch (e: Exception) {
                    currentStrokeId = null
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val id = currentStrokeId ?: return false
                
                // Prediction Playback
                val predicted = predictor?.predict()
                try {
                    if (predicted != null) {
                         inputView.addToStroke(event, pointerId, id, predicted)
                         predicted.recycle()
                    } else {
                         inputView.addToStroke(event, pointerId, id)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
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
                 val id = currentStrokeId ?: return false
                 inputView.cancelStroke(id, event)
                 currentStrokeId = null
                 return true
            }
        }
        return false
    }

    private fun createDefaultBrush(): Brush {
        return Brush.createWithColorIntArgb(
            family = StockBrushes.markerV1,
            colorIntArgb = android.graphics.Color.BLACK,
            size = 5f,
            epsilon = 0.1f
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true // Capture all
    }
}
