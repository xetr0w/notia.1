package com.notia.app.features.drawing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.PencilKit.PKCanvasView
import platform.PencilKit.PKInkingTool
import platform.PencilKit.PKInkingToolTypePen
import platform.PencilKit.PKInkingToolTypeMarker
import platform.PencilKit.PKInkingToolTypePencil

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DrawingCanvas(
    currentColor: Color,
    currentWidth: Float,
    modifier: Modifier
) {
    // Convert Compose Color to UIColor
    // Note: Simple conversion for now. Alpha is 0-1 in Compose.
    val uiColor = remember(currentColor) {
        UIColor(
            red = currentColor.red.toDouble(),
            green = currentColor.green.toDouble(),
            blue = currentColor.blue.toDouble(),
            alpha = currentColor.alpha.toDouble()
        )
    }

    UIKitView(
        factory = {
            val canvas = PKCanvasView()
            canvas.allowsFingerDrawing = true
            // Default tool
            canvas.tool = PKInkingTool(PKInkingToolTypePen, color = uiColor, width = currentWidth.toDouble())
            canvas
        },
        update = { canvas ->
            // Update tool on state change
            // Using Pen for default MVP, can switch based on other params later
            canvas.tool = PKInkingTool(PKInkingToolTypePen, color = uiColor, width = currentWidth.toDouble())
        },
        modifier = modifier
    )
}
