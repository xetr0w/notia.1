package com.notia.app

import androidx.compose.ui.window.ComposeUIViewController
import com.notia.app.di.ServiceLocator
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    ServiceLocator.init()
    return ComposeUIViewController { App() }
}
