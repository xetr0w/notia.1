package com.notia.app.drawing.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.ink.strokes.Stroke
import com.notia.app.data.DrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrawingViewModel(
    private val repository: DrawingRepository,
    private val currentNoteId: String = "default_note" // Placeholder ID
) : ViewModel() {

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    init {
        loadStrokes()
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
}
