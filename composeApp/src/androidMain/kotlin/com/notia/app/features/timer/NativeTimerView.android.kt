package com.notia.app.features.timer

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
actual fun NativeTimerView(
    modifier: Modifier,
    isRunning: Boolean,
    progress: Float
) {
    // Android implementation: Custom Compose Drawing mimicking "Native" feel
    Canvas(modifier = modifier) {
        drawCircle(
            color = Color.LightGray,
            style = Stroke(width = 20f)
        )
        drawArc(
            color = if (isRunning) Color.Blue else Color.Red,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = 20f)
        )
    }
}
