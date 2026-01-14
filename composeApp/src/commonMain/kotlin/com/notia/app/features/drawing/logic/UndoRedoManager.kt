package com.notia.app.features.drawing.logic

import com.notia.app.features.drawing.model.Stroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UndoRedoManager {
    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    private val undoStack = ArrayDeque<List<Stroke>>()
    private val redoStack = ArrayDeque<List<Stroke>>()

    private val maxHistorySize = 50

    fun addStroke(stroke: Stroke) {
        // Save current state to undo stack before adding
        if (undoStack.size >= maxHistorySize) {
            undoStack.removeFirst()
        }
        undoStack.addLast(_strokes.value)
        redoStack.clear()

        // Update state
        val newList = _strokes.value.toMutableList()
        newList.add(stroke)
        _strokes.value = newList
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previousState = undoStack.removeLast()
            
            // Save current to redo
            redoStack.addLast(_strokes.value)
            
            _strokes.value = previousState
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeLast()
            
            // Save current to undo
            undoStack.addLast(_strokes.value)
            
            _strokes.value = nextState
        }
    }
    
    fun clear() {
        undoStack.clear()
        redoStack.clear()
        _strokes.value = emptyList()
    }
    
    fun setStrokes(newStrokes: List<Stroke>) {
        clear()
        _strokes.value = newStrokes
    }
    
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
}
