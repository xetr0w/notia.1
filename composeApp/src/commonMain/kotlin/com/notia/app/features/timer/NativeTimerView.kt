package com.notia.app.features.timer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeTimerView(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    progress: Float
)
