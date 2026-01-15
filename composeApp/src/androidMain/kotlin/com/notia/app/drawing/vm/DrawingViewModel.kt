package com.notia.app.drawing.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.ink.strokes.Stroke
import com.notia.app.data.DrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DrawingTool {
    PEN, HIGHLIGHTER, ERASER
}

class DrawingViewModel(
    private val repository: DrawingRepository,
    private val currentNoteId: String = "default_note" // Placeholder ID
) : ViewModel() {

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()
    
    // UI State
    private val _activeTool = MutableStateFlow(DrawingTool.PEN)
    val activeTool: StateFlow<DrawingTool> = _activeTool.asStateFlow()
    
    private val _isStylusOnly = MutableStateFlow(false)
    val isStylusOnly: StateFlow<Boolean> = _isStylusOnly.asStateFlow()
    
    // Tool Properties
    private val _penColor = MutableStateFlow(androidx.compose.ui.graphics.Color.Black)
    val penColor = _penColor.asStateFlow()
    
    private val _penWidth = MutableStateFlow(5f)
    val penWidth = _penWidth.asStateFlow()
    
    private val _highlighterColor = MutableStateFlow(androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.4f))
    val highlighterColor = _highlighterColor.asStateFlow()
    
    private val _highlighterWidth = MutableStateFlow(20f)
    val highlighterWidth = _highlighterWidth.asStateFlow()

    // Debug State
    private val _isDebugMode = MutableStateFlow(false)
    val isDebugMode = _isDebugMode.asStateFlow()

    private val _debugRawPoints = MutableStateFlow<List<com.notia.app.drawing.engine.DrawingEngine.DebugPointInfo>>(emptyList())
    val debugRawPoints = _debugRawPoints.asStateFlow()
    
    private val _debugSampleRate = MutableStateFlow("Wait...")
    val debugSampleRate = _debugSampleRate.asStateFlow()

    private val _debugFreezeMode = MutableStateFlow(false)
    val debugFreezeMode = _debugFreezeMode.asStateFlow()

    init {
        loadStrokes()
    }
    
    fun setTool(tool: DrawingTool) {
        _activeTool.value = tool
    }
    
    fun toggleStylusOnly() {
        _isStylusOnly.value = !_isStylusOnly.value
    }
    
    fun setPenColor(color: androidx.compose.ui.graphics.Color) {
        _penColor.value = color
    }
    
    fun setPenWidth(width: Float) {
        _penWidth.value = width
    }
    
    fun setHighlighterColor(color: androidx.compose.ui.graphics.Color) {
        _highlighterColor.value = color
    }
    
    fun setHighlighterWidth(width: Float) {
        _highlighterWidth.value = width
    }

    private fun loadStrokes() {
        viewModelScope.launch {
            repository.getStrokes(currentNoteId).collect { loadedStrokes ->
                _strokes.value = loadedStrokes
            }
        }
    }

    fun onStrokeAdded(stroke: Stroke) {
        viewModelScope.launch {
            // 1. Persistence
            repository.saveStroke(currentNoteId, stroke)
            
            // 2. Update Local State (if Repository flow doesn't auto-update, which Room usually does)
            // But relying on Room flow source-of-truth is better.
        }
    }
    
    fun clearCanvas() {
        viewModelScope.launch {
            repository.clearStrokes(currentNoteId)
        }
    }

    // Debug Actions
    fun toggleDebugMode() {
        _isDebugMode.value = !_isDebugMode.value
    }
    
    fun toggleFreezeMode() {
        _debugFreezeMode.value = !_debugFreezeMode.value
    }
    
    fun updateDebugStats(rate: String) {
        if (_isDebugMode.value) {
            _debugSampleRate.value = rate
        }
    }
    
    fun updateRawPoints(points: List<com.notia.app.drawing.engine.DrawingEngine.DebugPointInfo>) {
        if (_isDebugMode.value) {
             _debugRawPoints.value = points
        }
    }
}
