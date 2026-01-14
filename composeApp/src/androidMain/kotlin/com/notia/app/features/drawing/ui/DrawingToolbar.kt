package com.notia.app.features.drawing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawingToolbar(
    currentWidth: Float,
    currentColor: Color,
    onWidthChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit,
    onClear: () -> Unit,
    onEraserClick: () -> Unit,
    isEraserActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Tools Row
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onColorChange(Color.Black); if(isEraserActive) onEraserClick() }) {
                    Icon(Icons.Default.Edit, "Pen", tint = if(!isEraserActive) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                }
                IconButton(onClick = onEraserClick) {
                    Icon(Icons.Default.CleaningServices, "Eraser", tint = if(isEraserActive) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, "Clear")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Slider Row for Width
            if (!isEraserActive) {
                Text("Thickness: ${currentWidth.toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = currentWidth,
                    onValueChange = onWidthChange,
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Colors Row
            if (!isEraserActive) {
                val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorChange(color) }
                                .then(if (currentColor == color) Modifier.background(Color.White.copy(alpha=0.3f)) else Modifier)
                        )
                    }
                }
            }
        }
    }
}
