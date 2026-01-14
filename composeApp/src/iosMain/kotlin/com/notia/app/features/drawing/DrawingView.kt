package com.notia.app.features.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.backgroundColor
import platform.CoreGraphics.CGRect

import com.notia.app.features.drawing.logic.UndoRedoManager
import com.notia.app.features.drawing.model.ToolConfig

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DrawingView(
    modifier: Modifier,
    toolConfig: ToolConfig,
    undoRedoManager: UndoRedoManager,
    onDraw: (Any) -> Unit
) {
    UIKitView(
        factory = {
            val view = UIView()
            view.backgroundColor = UIColor.whiteColor
            // TODO: Implement full Touch Handling and CoreGraphics drawing for iOS in next iteration.
            // For Step 4 Checkpoint 1, we ensure it compiles and shows a view.
            view
        },
        modifier = modifier
    )
}
