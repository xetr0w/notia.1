package com.notia.app.features.drawing.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.notia.app.drawing.vm.DrawingTool
import com.notia.app.drawing.vm.DrawingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingToolbar(
    viewModel: DrawingViewModel,
    modifier: Modifier = Modifier
) {
    val activeTool by viewModel.activeTool.collectAsState()
    val isStylusOnly by viewModel.isStylusOnly.collectAsState()
    
    // Tool Properties
    val penColor by viewModel.penColor.collectAsState()
    val penWidth by viewModel.penWidth.collectAsState()
    val highlighterColor by viewModel.highlighterColor.collectAsState()
    val highlighterWidth by viewModel.highlighterWidth.collectAsState()

    var showSettingsShortsheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Toolbar Surface
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Stylus Mode Toggle
                IconButton(
                    onClick = { viewModel.toggleStylusOnly() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isStylusOnly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (isStylusOnly) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = if (isStylusOnly) Icons.Default.EditOff else Icons.Default.Edit, // Icon representing "Touch Drawing" status
                        contentDescription = "Stylus Only Mode"
                    )
                }
                
                VerticalDivider(modifier = Modifier.height(24.dp))

                // 2. Tools (Pen, Highlighter, Eraser)
                ToolButton(
                    icon = Icons.Default.Create,
                    label = "Pen",
                    isSelected = activeTool == DrawingTool.PEN,
                    onClick = { 
                        if (activeTool == DrawingTool.PEN) {
                            showSettingsShortsheet = !showSettingsShortsheet
                        } else {
                            viewModel.setTool(DrawingTool.PEN)
                            showSettingsShortsheet = true 
                        }
                    }
                )

                ToolButton(
                    icon = Icons.Default.Highlight,
                    label = "Highlighter",
                    isSelected = activeTool == DrawingTool.HIGHLIGHTER,
                    onClick = { 
                        if (activeTool == DrawingTool.HIGHLIGHTER) {
                            showSettingsShortsheet = !showSettingsShortsheet
                        } else {
                            viewModel.setTool(DrawingTool.HIGHLIGHTER)
                            showSettingsShortsheet = true
                        }
                    }
                )

                ToolButton(
                    icon = Icons.Default.CleaningServices, // Eraser icon
                    label = "Eraser",
                    isSelected = activeTool == DrawingTool.ERASER,
                    onClick = { 
                        viewModel.setTool(DrawingTool.ERASER)
                        showSettingsShortsheet = false
                    }
                )
                
                VerticalDivider(modifier = Modifier.height(24.dp))
                
                // 3. Clear Canvas
                IconButton(onClick = { viewModel.clearCanvas() }) {
                    Icon(Icons.Default.Delete, "Clear All")
                }
            }
        }

        // Settings Sheet (Color & Size) for Active Tool
        AnimatedVisibility(
            visible = showSettingsShortsheet && activeTool != DrawingTool.ERASER
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Size Slider
                    val currentWidth = if (activeTool == DrawingTool.PEN) penWidth else highlighterWidth
                    val maxWidth = if (activeTool == DrawingTool.PEN) 50f else 100f
                    
                    Text("Thickness: ${currentWidth.toInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = currentWidth,
                        onValueChange = { 
                            if (activeTool == DrawingTool.PEN) viewModel.setPenWidth(it)
                            else viewModel.setHighlighterWidth(it)
                        },
                        valueRange = 1f..maxWidth
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Color Palette
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    val colors = listOf(
                        Color.Black, Color.Red, Color.Blue, Color.Green, 
                        Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray,
                        Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFB00020)
                    )
                    
                    val currentColor = if (activeTool == DrawingTool.PEN) penColor else highlighterColor
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(colors) { color ->
                            // For highlighter, apply alpha to display
                            val displayColor = if (activeTool == DrawingTool.HIGHLIGHTER) color.copy(alpha = 0.4f) else color
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(displayColor)
                                    .border(
                                        width = if (currentColor.toArgb() == displayColor.toArgb()) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (activeTool == DrawingTool.PEN) viewModel.setPenColor(color)
                                        else viewModel.setHighlighterColor(color.copy(alpha = 0.4f))
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
