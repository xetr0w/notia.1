package com.notia.app.drawing.engine

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.ink.brush.Brush
import androidx.ink.strokes.Stroke
import com.notia.app.drawing.engine.ink.InkRenderer
import com.notia.app.drawing.engine.ink.InkStrokeAuthor

/**
 * The facade for the Drawing Engine.
 * Combines Input (Author) and Output (Renderer) layers.
 */
class DrawingEngine(context: Context) : FrameLayout(context) {

    val renderer = InkRenderer(context)
    val author = InkStrokeAuthor(context)
    
    // Callback to VM/UI -> Stroke Created
    private var onStrokeCreatedListener: ((Stroke) -> Unit)? = null
    
    // Matrix for Zoom/Pan
    private var currentMatrix = android.graphics.Matrix()

    init {
        // Layer order: Renderer (Bottom) -> Author (Top)
        addView(renderer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(author, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        
        author.setOnStrokeCommittedListener { stroke ->
            // 1. Add to local renderer immediately for "no-flicker"
            renderer.addStroke(stroke)
            
            // 2. Notify up for persistence/undo-stack
            onStrokeCreatedListener?.invoke(stroke)
        }
    }
    
    fun setMatrix(matrix: android.graphics.Matrix) {
        currentMatrix.set(matrix)
        // Propagate to sub-views if they need it for rendering optimization
        // For Ink API, we mainly need it for the renderer:
        renderer.setTransform(matrix)
        // And authoring needs it to map touch correctly if visual scale changes?
        // Actually, Ink Authoring usually handles raw touch.
        // If we scale the VIEW (FrameLayout), then Authoring scales with it.
        // Let's rely on View scale for now or pass matrix to Renderer.
        this.scaleX = matrix.values()[android.graphics.Matrix.MSCALE_X]
        this.scaleY = matrix.values()[android.graphics.Matrix.MSCALE_Y]
        this.translationX = matrix.values()[android.graphics.Matrix.MTRANS_X]
        this.translationY = matrix.values()[android.graphics.Matrix.MTRANS_Y]
    }
    
    // Helper to extract values
    private fun android.graphics.Matrix.values(): FloatArray {
        val f = FloatArray(9)
        this.getValues(f)
        return f
    }
    
    fun setEraserActive(isActive: Boolean) {
        // TODO: Implement Eraser Mode logic in Author
        // author.setEraserMode(isActive)
    }
    
    fun setOnStrokeCreatedListener(listener: (Stroke) -> Unit) {
        onStrokeCreatedListener = listener
    }
    
    fun updateBrush(brush: Brush) {
        author.updateBrush(brush)
    }
    
    fun setStrokes(strokes: List<Stroke>) {
        renderer.setStrokes(strokes)
    }

    // Forward touch events to Author
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Give author first dibs
        val consumed = author.handleTouchEvent(ev)
        if (consumed) return true
        
        return super.dispatchTouchEvent(ev)
    }
}
