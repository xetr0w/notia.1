package com.notia.app.features.timer

import platform.UIKit.UIViewController

object IosTimerProvider {
    var timerScreenFactory: ((onNavigateBack: () -> Unit) -> UIViewController)? = null
}
