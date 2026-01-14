package com.notia.app.features.timer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UILabel
import platform.UIKit.UIColor
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIFont

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeTimerView(
    modifier: Modifier,
    isRunning: Boolean,
    progress: Float
) {
    // Placeholder Swift UI integration
    UIKitView(
        factory = {
            val label = UILabel()
            label.text = "Native iOS View"
            label.textColor = UIColor.blackColor
            label.textAlignment = NSTextAlignmentCenter
            label.font = UIFont.systemFontOfSize(20.0)
            label.backgroundColor = UIColor.lightGrayColor
            label
        },
        update = { view ->
            (view as UILabel).text = if (isRunning) "Running: ${(progress*100).toInt()}%" else "Paused"
        },
        modifier = modifier
    )
}
