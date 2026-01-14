package com.notia.app.drawing.engine.ink

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke

/**
 * Responsible for rendering the "dry" (committed) ink.
 */
class InkRenderer(context: Context) : View(context) {
    
    private val renderer = CanvasStrokeRenderer.create()
    private val strokes = mutableListOf<Stroke>()
    private val drawMatrix = Matrix()

    fun setStrokes(newStrokes: List<Stroke>) {
        strokes.clear()
        strokes.addAll(newStrokes)
        invalidate()
    }
    
    fun addStroke(stroke: Stroke) {
        strokes.add(stroke)
        invalidate()
    }
    
    fun setTransform(matrix: Matrix) {
        drawMatrix.set(matrix)
        invalidate()
    }
    
    fun clear() {
        strokes.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokes.forEach { stroke ->
             renderer.draw(canvas, stroke, drawMatrix)
        }
    }
}
